package org.tamtamcatworks.auction.client.controller.admin.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsersManagementControllerTest {

    @Test
    void controllerShouldBeCreated() {

        UsersManagementController controller =
                new UsersManagementController();

        assertNotNull(controller);
    }
}