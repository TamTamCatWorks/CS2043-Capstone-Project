package org.tamtamcatworks.auction.client.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.shared.request.LoginRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

@Route(fxml = "/fxml/login.fxml", layout = Route.AUTH_LAYOUT)
public class LoginController extends BaseController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        messageLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError(messageLabel, "Please enter both email and password.");
            return;
        }

        setLoading(true);
        messageLabel.setText("");

        AsyncTask.<UserResponse>run(() -> api.login(new LoginRequest(email, password)))
            .onSuccess(user -> {
                setLoading(false);
                if (user != null) {
                    SessionManager.setCurrentUser(user);
                    if (user.isAdmin()) {
                        Navigation.navigateTo("/fxml/admin/dashboard/admin-dashboard.fxml");
                    } else {
                        Navigation.navigateTo("/fxml/auctions-list.fxml");
                    }
                } else {
                    showError(messageLabel, "Invalid response from server.");
                }
            })
            .onFailure(ex -> {
                setLoading(false);
                showError(messageLabel, "Login failed: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
            })
            .start();
    }

    @FXML
    private void handleGoToRegister() {
        Navigation.navigateTo("/fxml/register.fxml");
    }

    private void setLoading(boolean loading) {
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        loginButton.setDisable(loading);
        progressIndicator.setVisible(loading);
    }
}
