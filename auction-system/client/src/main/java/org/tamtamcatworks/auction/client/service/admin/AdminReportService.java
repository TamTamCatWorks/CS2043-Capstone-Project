package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.shared.response.AdminReportResponse;

public class AdminReportService extends BaseAdminService {

  public List<AdminReportResponse> getReports() {

    return apiClient
        .client()
        .get()
        .uri("/admin/reports")
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  public void resolveReport(String reportId) {

    apiClient
        .client()
        .patch()
        .uri("/admin/reports/{id}/resolve", reportId)
        .retrieve()
        .toBodilessEntity();
  }

  public void rejectReport(String reportId) {

    apiClient
        .client()
        .patch()
        .uri("/admin/reports/{id}/reject", reportId)
        .retrieve()
        .toBodilessEntity();
  }
}
