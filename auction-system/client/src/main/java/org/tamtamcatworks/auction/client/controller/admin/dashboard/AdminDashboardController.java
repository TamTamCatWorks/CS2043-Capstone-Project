package org.tamtamcatworks.auction.client.controller.admin.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.component.admin.analytics.AnalyticsChart;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.component.admin.dashboard.StatCard;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.util.admin.AdminPermissionGuard;

@Route(
    fxml = "/fxml/admin/dashboard/admin-dashboard.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml"
)
public class AdminDashboardController extends BaseController {

    private void buildAnalytics() {

        AnalyticsChart chart = new AnalyticsChart();

        java.util.Map<String, Number> userGrowth =
            java.util.Map.of(

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

    @FXML
    private HBox statsContainer;

    @FXML
    private Label systemOverviewLabel;

    @FXML
    private VBox analyticsContainer;

    @FXML
    public void initialize() {

        AdminPermissionGuard.requireAdmin();

        buildStatistics();

        loadOverview();

        buildAnalytics();
    }

    private void buildStatistics() {

        StatCard totalUsersCard =
                new StatCard(
                        "Total Users",
                        "125"
                );

        StatCard activeAuctionsCard =
                new StatCard(
                        "Active Auctions",
                        "32"
                );

        StatCard totalRevenueCard =
                new StatCard(
                        "Revenue",
                        "$12,500"
                );

        statsContainer.getChildren().addAll(

                totalUsersCard,
                activeAuctionsCard,
                totalRevenueCard
        );
    }

    private void loadOverview() {

        systemOverviewLabel.setText(

                """
                System status: ONLINE
                
                Active auctions are running normally.
                
                No critical alerts detected.
                """
        );
    }
}