package org.tamtamcatworks.auction.client.controller.shell;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.tamtamcatworks.auction.client.ApiClient;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

import java.util.List;

public class NotificationsViewController {
    @FXML private ListView<NotificationResponse> notificationsListView;
    @FXML private Label emptyLabel;

    private final ApiClient apiClient = SessionManager.getApiClient();

    @FXML
    public void initialize() {
        notificationsListView.setCellFactory(lv -> new NotificationListCell());
        loadNotifications();
    }

    private void loadNotifications() {
        Task<List<NotificationResponse>> task = new Task<>() {
            @Override
            protected List<NotificationResponse> call() {
                return apiClient.getNotifications();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<NotificationResponse> notes = task.getValue();
            if (notes == null || notes.isEmpty()) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
                notificationsListView.setVisible(false);
                notificationsListView.setManaged(false);
            } else {
                notificationsListView.getItems().setAll(notes);
            }
            try { apiClient.markNotificationsRead(); } catch (Exception ex) { }
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            emptyLabel.setText("Failed to load notifications");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }));
        new Thread(task, "load-notifications-view").start();
    }
}
