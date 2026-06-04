package org.tamtamcatworks.auction.client.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AdminUserServiceTest {

  private final AdminUserService service = new AdminUserService();

  @Test
  void serviceShouldBeCreated() {

    assertNotNull(service);
  }
}
