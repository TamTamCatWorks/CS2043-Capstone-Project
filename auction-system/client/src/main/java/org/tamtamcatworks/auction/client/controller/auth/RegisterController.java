package org.tamtamcatworks.auction.client.controller.auth;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField fullNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

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
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        setLoading(true);
        messageLabel.setText("");

        Task<UserResponse> registerTask = new Task<>() {
            @Override
            protected UserResponse call() throws Exception {
                RegisterRequest request = new RegisterRequest(username, email, password, fullName);
                return SessionManager.getApiClient().register(request);
            }
        };

        registerTask.setOnSucceeded(e -> {
            setLoading(false);
            UserResponse user = registerTask.getValue();
            if (user != null) {
                showSuccess("Registration successful! Redirecting to login...");
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(event -> Navigation.navigateTo("/fxml/login.fxml"));
                delay.play();
            } else {
                showError("Invalid response from server.");
            }
        });

        registerTask.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = registerTask.getException();
            showError("Registration failed: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
        });

        new Thread(registerTask).start();
    }

    @FXML
    private void handleGoToLogin() {
        Navigation.navigateTo("/fxml/login.fxml");
    }

    private void showError(String message) {
        messageLabel.getStyleClass().removeAll("success-label");
        if (!messageLabel.getStyleClass().contains("error-label")) {
            messageLabel.getStyleClass().add("error-label");
        }
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll("error-label");
        if (!messageLabel.getStyleClass().contains("success-label")) {
            messageLabel.getStyleClass().add("success-label");
        }
        messageLabel.setText(message);
    }

    private void setLoading(boolean loading) {
        usernameField.setDisable(loading);
        emailField.setDisable(loading);
        fullNameField.setDisable(loading);
        passwordField.setDisable(loading);
        registerButton.setDisable(loading);
        progressIndicator.setVisible(loading);
    }
}
