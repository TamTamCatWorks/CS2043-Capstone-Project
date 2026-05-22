package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class LayoutController {

    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem userNameItem;
    @FXML private MenuItem userEmailItem;
    @FXML private MenuItem notificationsHeaderItem;
    @FXML private SeparatorMenuItem notificationsSeparatorItem;
    @FXML private Button navAuctions;
    @FXML private Button navCreateAuction;

    private NotificationMenuManager notificationMenuManager;

    @FXML
    public void initialize() {
        UserResponse user = SessionManager.getCurrentUser();
        if (user != null) {
            userMenuButton.setText(user.fullName());
            userNameItem.setText(user.fullName());
            userEmailItem.setText(user.email());
        }

        notificationMenuManager = new NotificationMenuManager(
            userMenuButton, notificationsHeaderItem, notificationsSeparatorItem);

        // Set initial active tab based on current navigation context
        updateActiveTab("dashboard");

        // Populate the combined account menu and start polling notifications.
        notificationMenuManager.start();
    }

    // ── Navigation Handlers ──────────────────────────────────────────────────

    @FXML
    private void handleNavDashboard() {
        updateActiveTab("dashboard");
        Navigation.navigateTo("/fxml/dashboard.fxml");
    }

    @FXML
    private void handleNavAuctions() {
        updateActiveTab("auctions");
        Navigation.navigateTo("/fxml/auctions-list.fxml");
    }

    @FXML
    private void handleNavCreateAuction() {
        updateActiveTab("create");
        Navigation.navigateTo("/fxml/create-auction.fxml");
    }

    @FXML
    private void handleLogout() {
        notificationMenuManager.stop();
        SessionManager.logout();
        Navigation.navigateTo("/fxml/login.fxml");
    }

    @FXML
    private void handleUserMenuOpened() {
        notificationMenuManager.handleMenuOpened();
    }

    @FXML
    private void handleOpenNotificationsFromMenu() {
        // Request dashboard to open the Notifications view and navigate there
        SessionManager.setDashboardViewPath("/fxml/dashboard/notifications.fxml");
        Navigation.navigateTo("/fxml/dashboard.fxml");
    }

    // ── Tab Highlighting ─────────────────────────────────────────────────────

    private void updateActiveTab(String tab) {
        navAuctions.getStyleClass().remove("nav-tab-active");
        navCreateAuction.getStyleClass().remove("nav-tab-active");

        switch (tab) {
            case "auctions" -> navAuctions.getStyleClass().add("nav-tab-active");
            case "create" -> navCreateAuction.getStyleClass().add("nav-tab-active");
            default -> { }
        }
    }
}
