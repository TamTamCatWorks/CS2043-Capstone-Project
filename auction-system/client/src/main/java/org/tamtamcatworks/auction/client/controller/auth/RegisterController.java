package org.tamtamcatworks.auction.client.controller.auth;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

@Route(fxml = "/fxml/register.fxml", layout = Route.AUTH_LAYOUT)
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

        AsyncTask.<UserResponse>run(() ->
                SessionManager.getApiClient().register(new RegisterRequest(username, email, password, fullName)))
            .onSuccess(user -> {
                setLoading(false);
                if (user != null) {
                    showSuccess("Registration successful! Redirecting to login...");
                    PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                    delay.setOnFinished(event -> Navigation.navigateTo("/fxml/login.fxml"));
                    delay.play();
                } else {
                    showError("Invalid response from server.");
                }
            })
            .onFailure(ex -> {
                setLoading(false);
                showError("Registration failed: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
            })
            .start();
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
