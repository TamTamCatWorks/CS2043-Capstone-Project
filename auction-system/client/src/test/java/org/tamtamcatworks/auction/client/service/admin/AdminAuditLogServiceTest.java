package org.tamtamcatworks.auction.client.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AdminAuditLogServiceTest {

  private final AdminAuditLogService service = new AdminAuditLogService();

  @Test
  void serviceShouldBeCreated() {

    assertNotNull(service);
  }
}
