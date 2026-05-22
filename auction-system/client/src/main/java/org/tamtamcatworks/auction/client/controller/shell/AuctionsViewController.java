package org.tamtamcatworks.auction.client.controller.shell;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.tamtamcatworks.auction.client.ApiClient;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.List;

public class AuctionsViewController {
    @FXML private ListView<AuctionResponse> auctionsListView;
    @FXML private Label emptyLabel;

    private final ApiClient apiClient = SessionManager.getApiClient();

    @FXML
    public void initialize() {
        auctionsListView.setCellFactory(lv -> new AuctionListCell());
        if (!SessionManager.isLoggedIn()) return;
        loadMyAuctions(SessionManager.getCurrentUser());
    }

    private void loadMyAuctions(UserResponse user) {
        Task<List<AuctionResponse>> task = new Task<>() {
            @Override
            protected List<AuctionResponse> call() {
                return apiClient.getAllAuctions().stream()
                    .filter(a -> user.id().equals(a.sellerId()))
                    .toList();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<AuctionResponse> auctions = task.getValue();
            if (auctions == null || auctions.isEmpty()) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
                auctionsListView.setVisible(false);
                auctionsListView.setManaged(false);
            } else {
                auctionsListView.getItems().setAll(auctions);
            }
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            emptyLabel.setText("Failed to load auctions");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }));
        new Thread(task, "load-my-auctions-view").start();
    }
}
