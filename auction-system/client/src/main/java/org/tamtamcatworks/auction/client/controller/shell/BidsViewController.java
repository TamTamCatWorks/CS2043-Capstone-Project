package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.List;

/** Shows the current user's bid history in the dashboard home panel. */
public class BidsViewController {

  @FXML private ListView<BidResponse> bidsListView;
  @FXML private Label emptyLabel;

  @FXML
  public void initialize() {
    bidsListView.setCellFactory(lv -> new BidListCell());
    if (!SessionManager.isLoggedIn()) {
      return;
    }
    loadMyBids(SessionManager.getCurrentUser());
  }

  private void loadMyBids(UserResponse user) {
    AsyncTask.<List<BidResponse>>run(() -> {
      List<AuctionResponse> allAuctions = SessionManager.getApiClient().getAllAuctions();
      return allAuctions.stream()
          .flatMap(a -> {
            try {
              return SessionManager.getApiClient().getBids(a.id()).stream();
            } catch (Exception ex) {
              return java.util.stream.Stream.empty();
            }
          })
          .filter(b -> user.id().equals(b.bidderId()))
          .toList();
    })
        .onSuccess(bids -> {
          if (bids == null || bids.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            bidsListView.setVisible(false);
            bidsListView.setManaged(false);
          } else {
            bidsListView.getItems().setAll(bids);
          }
        })
        .onFailure(ex -> {
          emptyLabel.setText("Failed to load bids");
          emptyLabel.setVisible(true);
          emptyLabel.setManaged(true);
        })
        .start();
  }
}
