package org.tamtamcatworks.auction.client.controller.admin.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.component.admin.dashboard.DashboardStatCard;
import org.tamtamcatworks.auction.client.service.admin.AdminDashboardService;
import org.tamtamcatworks.auction.client.component.admin.analytics.AnalyticsChart;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.util.admin.AdminPermissionGuard;

@Route(
    fxml = "/fxml/admin/dashboard/admin-dashboard.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml"
)
public class AdminDashboardController extends BaseController {

    private void buildAnalytics() {

        AnalyticsChart chart = new AnalyticsChart();

        java.util.Map<String, Number> userGrowth = java.util.Map.of(
                    "Mon", 10,
                    "Tue", 20,
                    "Wed", 18,
                    "Thu", 30,
                    "Fri", 45
            );

        chart.addSeries(
                "Users",
                userGrowth
        );

        analyticsContainer.getChildren().add(chart);
    }

    private void loadAnalytics() {

        var dashboard = dashboardService.getDashboard();

        DashboardStatCard usersCard = new DashboardStatCard(
                    "Total Users",
                    String.valueOf(dashboard.totalUsers())
                );

        DashboardStatCard adminsCard = new DashboardStatCard(
                    "Total Admins",
                    String.valueOf(dashboard.totalAdmins())
                );

        DashboardStatCard auctionsCard = new DashboardStatCard(
                    "Total Auctions",
                    String.valueOf(dashboard.totalAuctions())
                );

        statsContainer.getChildren().setAll(
                        usersCard,
                        adminsCard,
                        auctionsCard
                );
        }
    private final AdminDashboardService dashboardService = new AdminDashboardService();

    @FXML
    private HBox statsContainer;

    @FXML
    private Label systemOverviewLabel;

    @FXML
    private VBox analyticsContainer;

    @FXML
    public void initialize() {

        AdminPermissionGuard.requireAdmin();

        loadOverview();

        buildAnalytics();

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
                    + dashboard.totalAuctions()
                );
        }
}