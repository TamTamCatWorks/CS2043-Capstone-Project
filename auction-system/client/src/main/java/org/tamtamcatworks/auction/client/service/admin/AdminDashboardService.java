package org.tamtamcatworks.auction.client.service.admin;

import org.tamtamcatworks.auction.shared.response.AdminDashboardResponse;

public class AdminDashboardService
        extends BaseAdminService {

    public AdminDashboardResponse
    getDashboardAnalytics() {

        return apiClient.client()
                .get()
                .uri("/admin/dashboard")
                .retrieve()
                .body(AdminDashboardResponse.class);
    }
}