/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.egeria.connectors.juxt.crux.repositoryconnector;

import org.odpi.openmetadata.frameworks.connectors.properties.beans.ConnectorType;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryConnectorProviderBase;

import java.util.ArrayList;
import java.util.List;

/**
 * In the Open Connector Framework (OCF), a ConnectorProvider is a factory for a specific type of connector.
 * The CruxOMRSRepositoryConnectorProvider is the connector provider for the CruxOMRSRepositoryConnector.
 * It extends OMRSRepositoryConnectorProviderBase which in turn extends the OCF ConnectorProviderBase.
 * ConnectorProviderBase supports the creation of connector instances.
 * <p>
 * The CruxOMRSRepositoryConnectorProvider must initialize ConnectorProviderBase with the Java class
 * name of the OMRS Connector implementation (by calling super.setConnectorClassName(className)).
 * Then the connector provider will work.
 * <br><br>
 * The configurationProperties can contain the following keys:
 * <ul>
 *     <li><code>cruxConfig</code>: a Crux configuration document that follows the JSON structure documented on
 *         Crux's site: https://opencrux.com/reference/21.01-1.14.0/configuration.html</li>
 *     <li><code>syncIndex</code>: a boolean indicating whether writes should be done synchronously (true, default) to
 *         ensure the index is up-to-date before returning, or asynchronously (false) to ensure that the transaction is
 *         recorded but that the index can be eventually consistent (faster writes)</li>
 * </ul><br>
 * For example:
 * <code>
 * {
 *   "class": "Connection",
 *   "connectorType": {
 *     "class": "ConnectorType",
 *     "connectorProviderClassName": "org.odpi.egeria.connectors.juxt.crux.repositoryconnector.CruxOMRSRepositoryConnectorProvider"
 *   },
 *   "configurationProperties": {
 *     "cruxConfig": {
 *       "crux/index-store": {
 *         "kv-store": {
 *           "crux/module": "crux.rocksdb/-&gt;kv-store",
 *           "db-dir": "data/servers/crux/rdb-index"
 *         }
 *       },
 *       "crux/document-store": {
 *         "kv-store": {
 *           "crux/module": "crux.rocksdb/-&gt;kv-store",
 *           "db-dir": "data/servers/crux/rdb-docs"
 *         }
 *       },
 *       "crux/tx-log": {
 *         "crux/module": "crux.kafka/-&gt;tx-log",
 *         "kafka-config": {
 *           "bootstrap-servers": "localhost:9092"
 *         },
 *         "tx-topic-opts": {
 *           "topic-name": "crux-tx-log"
 *         },
 *         "poll-wait-duration": "PT1S"
 *       }
 *     },
 *     "syncIndex": false
 *   }
 * }
 * </code>
 */
public class CruxOMRSRepositoryConnectorProvider extends OMRSRepositoryConnectorProviderBase {

    static final String CONNECTOR_TYPE_GUID = "33d8b72f-b3e4-45ac-a6b3-d1f547dc5320";
    static final String CONNECTOR_TYPE_NAME = "OMRS Crux Repository Connector";
    static final String CONNECTOR_TYPE_DESC = "OMRS Crux Repository Connector that uses Crux as a back-end historical repository for metadata.";

    public static final String CRUX_CONFIG = "cruxConfig";
    public static final String SYNCHRONOUS_INDEX = "syncIndex";

    /**
     * Constructor used to initialize the ConnectorProviderBase with the Java class name of the specific
     * OMRS Connector implementation.
     */
    public CruxOMRSRepositoryConnectorProvider() {

        Class<?> connectorClass = CruxOMRSRepositoryConnector.class;
        super.setConnectorClassName(connectorClass.getName());

        ConnectorType connectorType = new ConnectorType();
        connectorType.setType(ConnectorType.getConnectorTypeType());
        connectorType.setGUID(CONNECTOR_TYPE_GUID);
        connectorType.setQualifiedName(CONNECTOR_TYPE_NAME);
        connectorType.setDisplayName(CONNECTOR_TYPE_NAME);
        connectorType.setDescription(CONNECTOR_TYPE_DESC);
        connectorType.setConnectorProviderClassName(this.getClass().getName());

        List<String> configProperties = new ArrayList<>();
        configProperties.add(CRUX_CONFIG);
        configProperties.add(SYNCHRONOUS_INDEX);
        connectorType.setRecognizedConfigurationProperties(configProperties);

        super.connectorTypeBean = connectorType;

    }

}
