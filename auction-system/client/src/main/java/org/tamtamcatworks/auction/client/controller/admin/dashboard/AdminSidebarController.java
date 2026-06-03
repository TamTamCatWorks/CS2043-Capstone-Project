package org.tamtamcatworks.auction.client.controller.admin.dashboard;

import javafx.event.ActionEvent;

import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.Navigation;

public class AdminSidebarController
        extends BaseController {

    public void goToDashboard(ActionEvent event) {

        Navigation.navigateTo(
                "/fxml/admin/dashboard/admin-dashboard.fxml"
        );
    }

    public void goToUsers(ActionEvent event) {

        Navigation.navigateTo(
                "/fxml/admin/users/users-list.fxml"
        );
    }

    public void goToAuctions(ActionEvent event) {

        Navigation.navigateTo(
                "/fxml/admin/auctions/auctions-list.fxml"
        );
    }

    public void goToLogs(ActionEvent event) {

        Navigation.navigateTo(
                "/fxml/admin/logs/audit-logs.fxml"
        );
    }
}