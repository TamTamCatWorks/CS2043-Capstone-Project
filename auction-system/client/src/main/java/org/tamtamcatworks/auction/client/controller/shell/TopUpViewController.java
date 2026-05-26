package org.tamtamcatworks.auction.client.controller.shell;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.tamtamcatworks.auction.client.ApiClient;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.request.TopUpRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class TopUpViewController {

    @FXML private TextField amountField;
    @FXML private Label feedbackLabel;
    @FXML private Button submitButton;

    private final ApiClient apiClient = SessionManager.getApiClient();

    @FXML
    public void initialize() {
        // any visual initialization
    }

    @FXML
    private void handleSubmit() {
        String input = amountField.getText();
        if (input == null || input.trim().isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(input.trim());
            if (amount <= 0) {
                showError("Amount must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid amount format.");
            return;
        }

        submitButton.setDisable(true);
        feedbackLabel.setVisible(false);

        Task<UserResponse> task = new Task<>() {
            @Override
            protected UserResponse call() throws Exception {
                return apiClient.topUp(new TopUpRequest(amount));
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    UserResponse updatedUser = getValue();
                    SessionManager.setCurrentUser(updatedUser);
                    amountField.clear();
                    showSuccess("Top-up successful! Available balance: $" + String.format("%,.2f", updatedUser.balance()));
                    submitButton.setDisable(false);

                    // Re-trigger layout rebuild via dashboard
                    Label totalBalanceLabel = (Label) amountField.getScene().lookup("#totalBalanceLabel");
                    Label holdBalanceLabel = (Label) amountField.getScene().lookup("#holdBalanceLabel");
                    Label availableBalanceLabel = (Label) amountField.getScene().lookup("#availableBalanceLabel");
                    double available = updatedUser.balance();
                    double hold = updatedUser.holdBalance();
                    double total = available + hold;

                    if (totalBalanceLabel != null) {
                        totalBalanceLabel.setText(String.format("$%,.2f", total));
                    }
                    if (holdBalanceLabel != null) {
                        holdBalanceLabel.setText(String.format("$%,.2f", hold));
                    }
                    if (availableBalanceLabel != null) {
                        availableBalanceLabel.setText(String.format("$%,.2f", available));
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Failed to process top-up: " + getException().getMessage());
                    submitButton.setDisable(false);
                });
            }
        };

        new Thread(task, "top-up-task").start();
    }

    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #e74c3c;"); // red
        feedbackLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #2ecc71;"); // green
        feedbackLabel.setVisible(true);
    }
}
