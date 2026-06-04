package org.tamtamcatworks.auction.client.controller.admin.dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.auth.admin.AdminFeatureGate;
import org.tamtamcatworks.auction.client.auth.admin.AdminPermission;
import org.tamtamcatworks.auction.client.controller.BaseController;

public class AdminSidebarController extends BaseController {

  @FXML private Button usersButton;

  @FXML private Button auctionsButton;

  @FXML private Button logsButton;

  @FXML private Button viewAsUserButton;

  @FXML
  public void initialize() {

    AdminFeatureGate.requirePermission(usersButton, AdminPermission.MANAGE_USERS);

    AdminFeatureGate.requirePermission(auctionsButton, AdminPermission.MANAGE_AUCTIONS);

    AdminFeatureGate.requirePermission(logsButton, AdminPermission.VIEW_LOGS);
  }

  public void goToDashboard(ActionEvent event) {

    Navigation.navigateTo("/fxml/admin/dashboard/admin-dashboard.fxml");
  }

  public void goToNotifications(ActionEvent event) {

    Navigation.navigateTo("/fxml/admin/notification/admin-notifications.fxml");
  }

  public void goToUsers(ActionEvent event) {

    Navigation.navigateTo("/fxml/admin/users/users-list.fxml");
  }

  public void goToAuctions(ActionEvent event) {

    Navigation.navigateTo("/fxml/admin/auctions/auctions-list.fxml");
  }

  public void goToLogs(ActionEvent event) {

    Navigation.navigateTo("/fxml/admin/logs/audit-logs.fxml");
  }

  public void goToUserView(ActionEvent event) {

    Navigation.navigateTo("/fxml/auctions-list.fxml");
  }
}
