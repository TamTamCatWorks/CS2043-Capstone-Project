package org.tamtamcatworks.auction.client.service.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminDashboardServiceTest {

    private final AdminDashboardService service =
            new AdminDashboardService();

    @Test
    void serviceShouldBeCreated() {

        assertNotNull(service);
    }
}