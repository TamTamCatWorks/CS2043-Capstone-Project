package org.tamtamcatworks.auction.client.controller.admin.logs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogsControllerTest {

    @Test
    void controllerShouldBeCreated() {

        AuditLogsController controller =
                new AuditLogsController();

        assertNotNull(controller);
    }
}