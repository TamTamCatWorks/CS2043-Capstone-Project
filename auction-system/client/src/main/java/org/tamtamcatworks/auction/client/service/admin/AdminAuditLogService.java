package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;

public class AdminAuditLogService extends BaseAdminService {

  public List<AdminAuditLogResponse> getAuditLogs() {

    return get("/admin/audit-logs", new ParameterizedTypeReference<>() {});
  }
}
