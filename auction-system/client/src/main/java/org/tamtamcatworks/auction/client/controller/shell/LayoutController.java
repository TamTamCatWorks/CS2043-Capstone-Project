package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class LayoutController {

    @FXML
    private MenuButton userMenuButton;

    @FXML
    private MenuItem userNameItem;

    @FXML
    private MenuItem userEmailItem;

    @FXML
    private Button navAuctions;

    @FXML
    private Button navCreateAuction;

    @FXML
    public void initialize() {
        UserResponse user = SessionManager.getCurrentUser();
        if (user != null) {
            userMenuButton.setText(user.fullName());
            userNameItem.setText(user.fullName());
            userEmailItem.setText(user.email());
        }
        // Set initial active tab based on current navigation context
        updateActiveTab("dashboard");
    }

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
        SessionManager.logout();
        Navigation.navigateTo("/fxml/login.fxml");
    }

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
