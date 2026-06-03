package org.tamtamcatworks.auction.client.service.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminAuditLogServiceTest {

    private final AdminAuditLogService service =
            new AdminAuditLogService();

    @Test
    void serviceShouldBeCreated() {

        assertNotNull(service);
    }
}