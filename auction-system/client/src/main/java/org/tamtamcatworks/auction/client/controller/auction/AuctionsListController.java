package org.tamtamcatworks.auction.client.controller.auction;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.NavigationState;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

/** Controller for the auction browse/list view. */
@Route(fxml = "/fxml/auctions-list.fxml", layout = Route.DASHBOARD_LAYOUT)
public class AuctionsListController {

  @FXML private ComboBox<String> statusFilter;
  @FXML private StackPane loadingPane;
  @FXML private StackPane emptyPane;
  @FXML private Label errorLabel;
  @FXML private FlowPane auctionsContainer;

  private final List<AuctionResponse> loadedAuctions = new ArrayList<>();
  private String searchQuery = "";
  private String searchCategory = "All categories";

  @FXML
  public void initialize() {
    String pendingQuery = NavigationState.getPendingSearchQuery();
    String pendingCategory = NavigationState.getPendingSearchCategory();
    if (pendingQuery != null) {
      searchQuery = pendingQuery.trim();
    }
    if (pendingCategory != null && !pendingCategory.isBlank()) {
      searchCategory = pendingCategory.trim();
    }
    if (pendingQuery != null || pendingCategory != null) {
      NavigationState.clearPendingSearch();
    }

    statusFilter.getItems().addAll("ALL", "ACTIVE", "PENDING", "CLOSED");
    statusFilter.setValue("ALL");
    statusFilter.setOnAction(e -> loadAuctions());
    loadAuctions();
  }

  private void loadAuctions() {
    setLoading(true);
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    emptyPane.setVisible(false);
    emptyPane.setManaged(false);
    auctionsContainer.getChildren().clear();

    String selected = statusFilter.getValue();
    boolean hasSearch = (searchQuery != null && !searchQuery.isBlank())
        || (searchCategory != null && !searchCategory.isBlank()
            && !"All categories".equalsIgnoreCase(searchCategory));

    AsyncTask.<List<AuctionResponse>>run(() -> {
          if (hasSearch) {
            return SessionManager.getApiClient().searchAuctions(searchQuery, selected, searchCategory);
          }
          if ("ALL".equals(selected)) {
            return SessionManager.getApiClient().getAllAuctions();
          } else {
            return SessionManager.getApiClient().getAuctionsByStatus(selected);
          }
        })
        .onSuccess(auctions -> {
          setLoading(false);
          loadedAuctions.clear();
          if (auctions != null) {
            loadedAuctions.addAll(auctions);
          }

          if (loadedAuctions.isEmpty()) {
            emptyPane.setVisible(true);
            emptyPane.setManaged(true);
          } else {
            for (AuctionResponse auction : loadedAuctions) {
              auctionsContainer.getChildren().add(AuctionCardFactory.createAuctionCard(auction));
            }
          }
        })
        .onFailure(ex -> {
          setLoading(false);
          String msg = ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error";
          errorLabel.setText("Failed to load auctions: " + msg);
          errorLabel.setVisible(true);
          errorLabel.setManaged(true);
        })
        .start();
  }

  private void setLoading(boolean loading) {
    loadingPane.setVisible(loading);
    loadingPane.setManaged(loading);
  }

}
