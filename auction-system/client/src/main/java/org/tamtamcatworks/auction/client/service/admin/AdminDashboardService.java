package org.tamtamcatworks.auction.client.service.admin;

import org.tamtamcatworks.auction.shared.response.AdminDashboardResponse;

public class AdminDashboardService extends BaseAdminService {

  public AdminDashboardResponse getDashboard() {

    return get("/admin/dashboard", AdminDashboardResponse.class);
  }
}
