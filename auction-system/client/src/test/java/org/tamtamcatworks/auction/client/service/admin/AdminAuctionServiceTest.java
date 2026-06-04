package org.tamtamcatworks.auction.client.service.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminAuctionServiceTest {

    private final AdminAuctionService service =
            new AdminAuctionService();

    @Test
    void serviceShouldBeCreated() {

        assertNotNull(service);
    }

    @Test
    void searchShouldAcceptKeyword() {

        assertDoesNotThrow(() -> {

            service.searchAuctions("phone");
        });
    }
}