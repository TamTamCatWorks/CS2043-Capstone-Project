package org.tamtamcatworks.auction.client.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AdminDashboardServiceTest {

  private final AdminDashboardService service = new AdminDashboardService();

  @Test
  void serviceShouldBeCreated() {

    assertNotNull(service);
  }
}
