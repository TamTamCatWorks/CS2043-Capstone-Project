package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn()) {
            javafx.application.Platform.runLater(() -> Navigation.navigateTo("/fxml/login.fxml"));
            return;
        }

        UserResponse user = SessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome back, " + user.fullName() + "!");
        usernameLabel.setText("Username: " + user.username());
        emailLabel.setText("Email: " + user.email());
        balanceLabel.setText(String.format("Balance: $%.2f", user.balance()));
    }
}
