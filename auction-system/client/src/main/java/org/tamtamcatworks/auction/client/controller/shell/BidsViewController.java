package org.tamtamcatworks.auction.client.controller.shell;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.tamtamcatworks.auction.client.ApiClient;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.List;

public class BidsViewController {
    @FXML private ListView<BidResponse> bidsListView;
    @FXML private Label emptyLabel;

    private final ApiClient apiClient = SessionManager.getApiClient();

    @FXML
    public void initialize() {
        bidsListView.setCellFactory(lv -> new BidListCell());
        if (!SessionManager.isLoggedIn()) return;
        loadMyBids(SessionManager.getCurrentUser());
    }

    private void loadMyBids(UserResponse user) {
        Task<List<BidResponse>> task = new Task<>() {
            @Override
            protected List<BidResponse> call() {
                List<AuctionResponse> allAuctions = apiClient.getAllAuctions();
                return allAuctions.stream()
                    .flatMap(a -> {
                        try {
                            return apiClient.getBids(a.id()).stream();
                        } catch (Exception ex) {
                            return java.util.stream.Stream.empty();
                        }
                    })
                    .filter(b -> user.id().equals(b.bidderId()))
                    .toList();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<BidResponse> bids = task.getValue();
            if (bids == null || bids.isEmpty()) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
                bidsListView.setVisible(false);
                bidsListView.setManaged(false);
            } else {
                bidsListView.getItems().setAll(bids);
            }
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            emptyLabel.setText("Failed to load bids");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }));
        new Thread(task, "load-my-bids-view").start();
    }
}
