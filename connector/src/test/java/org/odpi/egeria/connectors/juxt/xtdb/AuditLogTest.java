/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.egeria.connectors.juxt.xtdb;

import org.odpi.egeria.connectors.juxt.xtdb.auditlog.XtdbOMRSAuditCode;
import org.odpi.egeria.connectors.juxt.xtdb.auditlog.XtdbOMRSErrorCode;
import org.odpi.openmetadata.frameworks.auditlog.messagesets.AuditLogMessageDefinition;
import org.odpi.openmetadata.frameworks.auditlog.messagesets.ExceptionMessageDefinition;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertFalse;

/**
 * Test the uniqueness of audit and error logging messages.
 */
public class AuditLogTest {

    @Test
    void testAuditCodeUniqueness() {

        Set<String> values = new HashSet<>();
        for (XtdbOMRSAuditCode a : XtdbOMRSAuditCode.values()) {
            AuditLogMessageDefinition b = a.getMessageDefinition();
            String messageId = b.getMessageId();
            assertFalse(values.contains(messageId), "Duplicate messageId found: " + messageId);
            values.add(messageId);
        }

    }

    @Test
    void testErrorCodeUniqueness() {

        Set<String> values = new HashSet<>();
        for (XtdbOMRSErrorCode a : XtdbOMRSErrorCode.values()) {
            ExceptionMessageDefinition b = a.getMessageDefinition();
            String messageId = b.getMessageId();
            assertFalse(values.contains(messageId), "Duplicate messageId found: " + messageId);
            values.add(messageId);
        }

    }

}
