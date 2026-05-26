package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

import java.util.List;

/** Shows the current user's notifications in the dashboard. */
public class NotificationsViewController {

  @FXML private ListView<NotificationResponse> notificationsListView;
  @FXML private Label emptyLabel;

  @FXML
  public void initialize() {
    notificationsListView.setCellFactory(lv -> new NotificationListCell());
    loadNotifications();
  }

  private void loadNotifications() {
    AsyncTask.<List<NotificationResponse>>run(() -> SessionManager.getApiClient().getNotifications())
        .onSuccess(notes -> {
          if (notes == null || notes.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            notificationsListView.setVisible(false);
            notificationsListView.setManaged(false);
          } else {
            notificationsListView.getItems().setAll(notes);
          }
          try {
            SessionManager.getApiClient().markNotificationsRead();
          } catch (Exception ex) {
            // Silently ignore mark-as-read failures
          }
        })
        .onFailure(ex -> {
          emptyLabel.setText("Failed to load notifications");
          emptyLabel.setVisible(true);
          emptyLabel.setManaged(true);
        })
        .start();
  }
}
