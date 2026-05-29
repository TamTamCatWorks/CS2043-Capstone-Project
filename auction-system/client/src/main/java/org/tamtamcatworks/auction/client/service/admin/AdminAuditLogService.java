package org.tamtamcatworks.auction.client.service.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;

public class AdminAuditLogService
        extends BaseAdminService {

    public List<AdminAuditLogResponse>
    getAuditLogs() {

        return apiClient.client()
            .get()
            .uri("/admin/audit-logs")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }
}