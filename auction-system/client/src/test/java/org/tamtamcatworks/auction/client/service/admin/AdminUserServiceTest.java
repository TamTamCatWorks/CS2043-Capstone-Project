package org.tamtamcatworks.auction.client.service.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserServiceTest {

    private final AdminUserService service =
            new AdminUserService();

    @Test
    void serviceShouldBeCreated() {

        assertNotNull(service);
    }
}