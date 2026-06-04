package org.tamtamcatworks.auction.client.controller.admin.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.component.admin.dashboard.DashboardStatCard;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.service.admin.AdminDashboardService;
import org.tamtamcatworks.auction.client.util.admin.AdminPermissionGuard;

@Route(
    fxml = "/fxml/admin/dashboard/admin-dashboard.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml")
public class AdminDashboardController extends BaseController {

  private void loadAnalytics() {

    var dashboard = dashboardService.getDashboard();

    DashboardStatCard usersCard =
        new DashboardStatCard("Total Users", String.valueOf(dashboard.totalUsers()));

    DashboardStatCard adminsCard =
        new DashboardStatCard("Total Admins", String.valueOf(dashboard.totalAdmins()));

    DashboardStatCard auctionsCard =
        new DashboardStatCard("Total Auctions", String.valueOf(dashboard.totalAuctions()));

    statsContainer.getChildren().setAll(usersCard, adminsCard, auctionsCard);
  }

  private final AdminDashboardService dashboardService = new AdminDashboardService();

  @FXML private HBox statsContainer;

  @FXML private Label systemOverviewLabel;

  @FXML
  public void initialize() {

    AdminPermissionGuard.requireAdmin();

    loadOverview();

    loadAnalytics();
  }

  private void loadOverview() {

    var dashboard = dashboardService.getDashboard();

    systemOverviewLabel.setText(
        "Users: "
            + dashboard.totalUsers()
            + "\nAdmins: "
            + dashboard.totalAdmins()
            + "\nAuctions: "
            + dashboard.totalAuctions());
  }
}
