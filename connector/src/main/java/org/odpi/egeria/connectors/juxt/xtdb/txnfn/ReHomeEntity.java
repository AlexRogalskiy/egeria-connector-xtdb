/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.egeria.connectors.juxt.xtdb.txnfn;

import clojure.lang.*;
import org.odpi.egeria.connectors.juxt.xtdb.cache.ErrorMessageCache;
import org.odpi.egeria.connectors.juxt.xtdb.auditlog.XtdbOMRSErrorCode;
import org.odpi.egeria.connectors.juxt.xtdb.mapping.EntityDetailMapping;
import org.odpi.egeria.connectors.juxt.xtdb.repositoryconnector.XtdbOMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.EntityNotKnownException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.InvalidParameterException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.RepositoryErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xtdb.api.TransactionInstant;
import xtdb.api.tx.Transaction;

/**
 * Transaction function for updating an entity's home repository.
 */
public class ReHomeEntity extends ReHomeInstance {

    private static final Logger log = LoggerFactory.getLogger(ReHomeEntity.class);

    public static final Keyword FUNCTION_NAME = Keyword.intern("egeria", "reHomeEntity");
    private static final String CLASS_NAME = ReHomeEntity.class.getName();
    private static final String METHOD_NAME = FUNCTION_NAME.toString();
    private static final String FN = "" +
            "(fn [ctx eid user mid nmid nmname] " +
            "    (let [db (xtdb.api/db ctx)" +
            "          tx-id (:tx-id db)" +
            "          existing (xtdb.api/entity db eid)" +
            "          updated (.doc (" + ReHomeEntity.class.getCanonicalName() + ". tx-id existing user eid mid nmid nmname))" +
            getTxnTimeCalculation("updated") + "]" +
            "         [[:xtdb.api/put updated txt]]))";

    private final IPersistentMap xtdbDoc;

    /**
     * Constructor used to execute the transaction function.
     * @param txId the transaction ID of this function invocation
     * @param existing XTDB document to re-home
     * @param userId doing the update
     * @param entityGUID of the entity to re-home
     * @param metadataCollectionId of the metadata collection in which the transaction is running
     * @param newMetadataCollectionId in which to re-home to the entity
     * @param newMetadataCollectionName in which to re-home the entity
     * @throws Exception on any error
     */
    public ReHomeEntity(Long txId,
                        PersistentHashMap existing,
                        String userId,
                        String entityGUID,
                        String metadataCollectionId,
                        String newMetadataCollectionId,
                        String newMetadataCollectionName)
            throws Exception {

        try {
            if (existing == null) {
                throw new EntityNotKnownException(XtdbOMRSErrorCode.ENTITY_NOT_KNOWN.getMessageDefinition(
                        entityGUID), CLASS_NAME, METHOD_NAME);
            } else {
                TxnValidations.nonProxyEntity(existing, entityGUID, CLASS_NAME, METHOD_NAME);
                TxnValidations.entityFromStore(entityGUID, existing, CLASS_NAME, METHOD_NAME);
                validate(existing, metadataCollectionId, CLASS_NAME, METHOD_NAME);
                xtdbDoc = reHomeInstance(userId, existing, newMetadataCollectionId, newMetadataCollectionName);
            }
        } catch (Exception e) {
            throw ErrorMessageCache.add(txId, e);
        }

    }

    /**
     * Change the home repository of the provided entity instance in the XTDB repository by pushing the transaction
     * down into the repository itself.
     * @param xtdb connectivity
     * @param userId doing the update
     * @param entityGUID of the entity on which to change the home repository
     * @param newMetadataCollectionId in which to re-home to the entity
     * @param newMetadataCollectionName in which to re-home the entity
     * @return EntityDetail the entity with the new home repository applied
     * @throws EntityNotKnownException if the entity cannot be found
     * @throws InvalidParameterException if the entity exists but cannot be re-homed (i.e. not a reference copy)
     * @throws RepositoryErrorException on any other error
     */
    public static EntityDetail transact(XtdbOMRSRepositoryConnector xtdb,
                                        String userId,
                                        String entityGUID,
                                        String newMetadataCollectionId,
                                        String newMetadataCollectionName)
            throws EntityNotKnownException, InvalidParameterException, RepositoryErrorException {
        String docId = EntityDetailMapping.getReference(entityGUID);
        Transaction.Builder tx = Transaction.builder();
        tx.invokeFunction(FUNCTION_NAME, docId, userId, xtdb.getMetadataCollectionId(), newMetadataCollectionId, newMetadataCollectionName);
        TransactionInstant results = xtdb.runTx(tx.build());
        try {
            return xtdb.getResultingEntity(docId, results, METHOD_NAME);
        } catch (EntityNotKnownException | InvalidParameterException | RepositoryErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryErrorException(XtdbOMRSErrorCode.UNKNOWN_RUNTIME_ERROR.getMessageDefinition(),
                    CLASS_NAME,
                    METHOD_NAME,
                    e);
        }
    }

    /**
     * Interface that returns the updated document to write-back from the transaction.
     * @return IPersistentMap giving the updated document in its entirety
     */
    public IPersistentMap doc() {
        log.debug("Entity being persisted: {}", xtdbDoc);
        return xtdbDoc;
    }

    /**
     * Create the transaction function within XTDB.
     * @param tx transaction through which to create the function
     */
    public static void create(Transaction.Builder tx) {
        createTransactionFunction(tx, FUNCTION_NAME, FN);
    }

}
