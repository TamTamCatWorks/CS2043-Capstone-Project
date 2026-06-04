package org.tamtamcatworks.auction.client.controller.admin.auctions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionsManagementControllerTest {

    @Test
    void controllerShouldBeCreated() {

        AuctionsManagementController controller =
                new AuctionsManagementController();

        assertNotNull(controller);
    }
}