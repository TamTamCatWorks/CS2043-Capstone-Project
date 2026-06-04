package org.tamtamcatworks.auction.client.controller.shell;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

/** Shows the current user's own auctions in the dashboard home panel. */
public class AuctionsViewController {

  @FXML private ListView<AuctionResponse> auctionsListView;
  @FXML private Label emptyLabel;

  @FXML
  public void initialize() {
    auctionsListView.setCellFactory(lv -> new AuctionListCell());
    if (!SessionManager.isLoggedIn()) {
      return;
    }
    loadMyAuctions(SessionManager.getCurrentUser());
  }

  private void loadMyAuctions(UserResponse user) {
    AsyncTask.<List<AuctionResponse>>run(
            () ->
                SessionManager.getApiClient().getAllAuctions().stream()
                    .filter(a -> user.id().equals(a.sellerId()))
                    .toList())
        .onSuccess(
            auctions -> {
              if (auctions == null || auctions.isEmpty()) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
                auctionsListView.setVisible(false);
                auctionsListView.setManaged(false);
              } else {
                auctionsListView.getItems().setAll(auctions);
              }
            })
        .onFailure(
            ex -> {
              emptyLabel.setText("Failed to load auctions");
              emptyLabel.setVisible(true);
              emptyLabel.setManaged(true);
            })
        .start();
  }
}
