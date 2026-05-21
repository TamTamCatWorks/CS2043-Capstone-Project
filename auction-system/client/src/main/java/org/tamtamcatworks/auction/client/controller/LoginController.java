package org.tamtamcatworks.auction.client.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.request.LoginRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class LoginController {

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
            showError("Please enter both email and password.");
            return;
        }

        setLoading(true);
        messageLabel.setText("");

        Task<UserResponse> loginTask = new Task<>() {
            @Override
            protected UserResponse call() throws Exception {
                LoginRequest request = new LoginRequest(email, password);
                return SessionManager.getApiClient().login(request);
            }
        };

        loginTask.setOnSucceeded(e -> {
            setLoading(false);
            UserResponse user = loginTask.getValue();
            if (user != null) {
                SessionManager.setCurrentUser(user);
                Navigation.navigateTo("/fxml/dashboard.fxml");
            } else {
                showError("Invalid response from server.");
            }
        });

        loginTask.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = loginTask.getException();
            showError("Login failed: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
        });

        new Thread(loginTask).start();
    }

    @FXML
    private void handleGoToRegister() {
        Navigation.navigateTo("/fxml/register.fxml");
    }

    private void showError(String message) {
        messageLabel.getStyleClass().removeAll("success-label");
        if (!messageLabel.getStyleClass().contains("error-label")) {
            messageLabel.getStyleClass().add("error-label");
        }
        messageLabel.setText(message);
    }

    private void setLoading(boolean loading) {
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        loginButton.setDisable(loading);
        progressIndicator.setVisible(loading);
    }
}
