/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.egeria.connectors.juxt.xtdb.txnfn;

import clojure.lang.*;
import org.odpi.egeria.connectors.juxt.xtdb.auditlog.ErrorMessaging;
import org.odpi.egeria.connectors.juxt.xtdb.auditlog.XtdbOMRSErrorCode;
import org.odpi.egeria.connectors.juxt.xtdb.mapping.RelationshipMapping;
import org.odpi.egeria.connectors.juxt.xtdb.repositoryconnector.XtdbOMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.Relationship;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.InvalidParameterException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.RelationshipNotKnownException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.RepositoryErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xtdb.api.TransactionInstant;
import xtdb.api.tx.Transaction;

/**
 * Transaction function for soft-deleting a relationship.
 */
public class DeleteRelationship extends AbstractTransactionFunction {

    private static final Logger log = LoggerFactory.getLogger(DeleteRelationship.class);

    public static final Keyword FUNCTION_NAME = Keyword.intern("egeria", "deleteRelationship");
    private static final String METHOD_NAME = FUNCTION_NAME.toString();
    private static final String FN = "" +
            "(fn [ctx rid user] " +
            "    (let [db (xtdb.api/db ctx)" +
            "          tx-id (:tx-id db)" +
            "          existing (xtdb.api/entity db rid)" +
            "          deleted (.doc (" + DeleteRelationship.class.getCanonicalName() + ". tx-id existing user rid))]" +
            "         [[:xtdb.api/put deleted]]))";

    private final IPersistentMap xtdbDoc;

    /**
     * Constructor used to execute the transaction function.
     * @param txId the transaction ID of this function invocation
     * @param existing XTDB document to update
     * @param userId doing the deletion
     * @param obsoleteRelationshipGUID of the relationship to delete
     * @throws Exception on any error
     */
    public DeleteRelationship(Long txId,
                              PersistentHashMap existing,
                              String userId,
                              String obsoleteRelationshipGUID)
            throws Exception {

        try {
            if (existing == null) {
                throw new RelationshipNotKnownException(XtdbOMRSErrorCode.RELATIONSHIP_NOT_KNOWN.getMessageDefinition(
                        obsoleteRelationshipGUID), this.getClass().getName(), METHOD_NAME);
            } else {
                TxnUtils.validateInstanceIsNotDeleted(existing, obsoleteRelationshipGUID, this.getClass().getName(), METHOD_NAME);
                xtdbDoc = TxnUtils.deleteInstance(userId, existing);
            }
        } catch (Exception e) {
            throw ErrorMessaging.add(txId, e);
        }

    }

    /**
     * Soft-delete the specified relationship by pushing down the transaction.
     * @param xtdb connectivity
     * @param userId doing the deletion
     * @param relationshipGUID of the relationship to be deleted
     * @return the resulting deleted relationship
     * @throws RelationshipNotKnownException if the relationship cannot be found
     * @throws InvalidParameterException if the relationship exists but is already soft-deleted
     * @throws RepositoryErrorException on any other error
     */
    public static Relationship transact(XtdbOMRSRepositoryConnector xtdb,
                                        String userId,
                                        String relationshipGUID)
            throws RelationshipNotKnownException, InvalidParameterException, RepositoryErrorException {
        String docId = RelationshipMapping.getReference(relationshipGUID);
        Transaction.Builder tx = Transaction.builder();
        tx.invokeFunction(FUNCTION_NAME, docId, userId);
        TransactionInstant results = xtdb.runTx(tx.build());
        try {
            return xtdb.getResultingRelationship(docId, results, METHOD_NAME);
        } catch (RelationshipNotKnownException | InvalidParameterException | RepositoryErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryErrorException(XtdbOMRSErrorCode.UNKNOWN_RUNTIME_ERROR.getMessageDefinition(),
                    DeleteEntity.class.getName(),
                    METHOD_NAME,
                    e);
        }
    }


    /**
     * Interface that returns the updated document to write-back from the transaction.
     * @return IPersistentMap giving the updated document in its entirety
     */
    public IPersistentMap doc() {
        log.debug("deleteRelationship being persisted: {}", xtdbDoc);
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
