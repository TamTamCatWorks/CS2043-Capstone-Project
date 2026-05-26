package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.request.TopUpRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

/**
 * Controller for the balance top-up form.
 *
 * <p>After a successful top-up, simply calls
 * {@link SessionManager#setCurrentUser(UserResponse)} — the
 * {@code DashboardController} reacts via its
 * {@link SessionManager#currentUserProperty()} listener and updates the
 * balance labels automatically. No {@code scene.lookup()} required.
 */
public class TopUpViewController {

  @FXML private TextField amountField;
  @FXML private Label feedbackLabel;
  @FXML private Button submitButton;

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

    AsyncTask.<UserResponse>run(() -> SessionManager.getApiClient().topUp(new TopUpRequest(amount)))
        .onSuccess(updatedUser -> {
          // Updating the session property triggers DashboardController's listener
          SessionManager.setCurrentUser(updatedUser);
          amountField.clear();
          showSuccess("Top-up successful! Available: $" + String.format("%,.2f", updatedUser.balance()));
          submitButton.setDisable(false);
        })
        .onFailure(ex -> {
          showError("Failed to process top-up: " + ex.getMessage());
          submitButton.setDisable(false);
        })
        .start();
  }

  private void showError(String message) {
    feedbackLabel.setText(message);
    feedbackLabel.setStyle("-fx-text-fill: #e74c3c;");
    feedbackLabel.setVisible(true);
  }

  private void showSuccess(String message) {
    feedbackLabel.setText(message);
    feedbackLabel.setStyle("-fx-text-fill: #2ecc71;");
    feedbackLabel.setVisible(true);
  }
}
