package org.tamtamcatworks.auction.client.controller;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.tamtamcatworks.auction.client.ApiClient;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;

/**
 * Optional abstract base for page controllers, providing common concerns:
 * <ul>
 *   <li>Pre-wired {@link #api} field — no need to call
 *       {@code SessionManager.getApiClient()} in every subclass.</li>
 *   <li>{@link #requireAuth()} — redirects unauthenticated users to login.</li>
 *   <li>{@link #showError(Label, String)} / {@link #showSuccess(Label, String)}
 *       — consistent label state management.</li>
 *   <li>{@link #hideLabel(Label)} — hides a feedback label.</li>
 * </ul>
 *
 * <p>Subclasses declare their own {@code @FXML} fields normally; this base
 * class carries no FXML bindings of its own.
 */
public abstract class BaseController {

  /** Pre-wired reference to the shared {@link ApiClient}. */
  protected final ApiClient api = SessionManager.getApiClient();

  /**
   * Redirect to the login screen if the user is not authenticated.
   * Safe to call from {@code initialize()} — uses {@code Platform.runLater}
   * if called before the scene is fully shown.
   *
   * @return {@code true} if the user is authenticated, {@code false} if a
   *         redirect was triggered
   */
  protected boolean requireAuth() {
    if (!SessionManager.isLoggedIn()) {
      Platform.runLater(() -> Navigation.navigateTo("/fxml/login.fxml"));
      return false;
    }
    return true;
  }

  /**
   * Display an error message on the given label and make it visible.
   *
   * @param label   the feedback label (may already be styled via CSS class)
   * @param message the error message to display
   */
  protected void showError(Label label, String message) {
    if (label == null) {
      return;
    }
    label.getStyleClass().removeAll("success-label");
    if (!label.getStyleClass().contains("error-label")) {
      label.getStyleClass().add("error-label");
    }
    label.setText(message);
    label.setVisible(true);
    label.setManaged(true);
  }

  /**
   * Display a success message on the given label and make it visible.
   *
   * @param label   the feedback label
   * @param message the success message to display
   */
  protected void showSuccess(Label label, String message) {
    if (label == null) {
      return;
    }
    label.getStyleClass().removeAll("error-label");
    if (!label.getStyleClass().contains("success-label")) {
      label.getStyleClass().add("success-label");
    }
    label.setText(message);
    label.setVisible(true);
    label.setManaged(true);
  }

  /**
   * Hide a feedback label and remove it from layout flow.
   *
   * @param label the label to hide
   */
  protected void hideLabel(Label label) {
    if (label == null) {
      return;
    }
    label.setVisible(false);
    label.setManaged(false);
  }
}
