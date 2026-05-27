package org.tamtamcatworks.auction.client.controller.auction;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.NavigationState;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.PageResponse;

@Route(fxml = "/fxml/search-results.fxml", layout = Route.DASHBOARD_LAYOUT)
public class SearchResultsController {

  @FXML private ComboBox<String> statusFilter;
  @FXML private ComboBox<String> categoryFilter;
  @FXML private StackPane loadingPane;
  @FXML private StackPane emptyPane;
  @FXML private Label errorLabel;
  @FXML private FlowPane auctionsContainer;
  @FXML private Label subtitleLabel;
  @FXML private Button prevPageButton;
  @FXML private Button nextPageButton;
  @FXML private Label pageInfoLabel;
  @FXML private ComboBox<String> pageSizeCombo;

  private List<AuctionResponse> results;
  private int currentPage = 0;
  private int pageSize = 20;
  private int totalPages = 1;
  private long totalElements = 0;

  @FXML
  public void initialize() {
    statusFilter.getItems().addAll("ALL", "ACTIVE", "PENDING", "CLOSED");
    statusFilter.setValue("ALL");
    statusFilter.setOnAction(e -> {
      currentPage = 0;
      runSearch();
    });

    categoryFilter.getItems().addAll("All categories", "Art", "Electronics", "Vehicle", "Other");
    String pendingCategory = NavigationState.getPendingSearchCategory();
    categoryFilter.setValue(pendingCategory != null && !pendingCategory.isBlank()
        ? pendingCategory
        : "All categories");
    categoryFilter.setOnAction(e -> {
      currentPage = 0;
      runSearch();
    });

    pageSizeCombo.getItems().addAll("10", "20", "50");
    pageSizeCombo.setValue("20");
    pageSizeCombo.setOnAction(e -> {
      try {
        pageSize = Integer.parseInt(pageSizeCombo.getValue());
      } catch (Exception ex) {
        pageSize = 20;
      }
      currentPage = 0;
      runSearch();
    });

    prevPageButton.setOnAction(e -> {
      if (currentPage > 0) {
        currentPage -= 1;
        runSearch();
      }
    });

    nextPageButton.setOnAction(e -> {
      if (currentPage + 1 < totalPages) {
        currentPage += 1;
        runSearch();
      }
    });

    runSearch();
  }

  private void runSearch() {
    setLoading(true);
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    emptyPane.setVisible(false);
    emptyPane.setManaged(false);
    String pendingQuery = NavigationState.getPendingSearchQuery();
    String pendingCategory = categoryFilter != null ? categoryFilter.getValue() : NavigationState.getPendingSearchCategory();
    if (pendingQuery == null) pendingQuery = "";
    if (pendingCategory == null) pendingCategory = "";

    subtitleLabel.setText("Results for \"" + pendingQuery + "\"");

    final String q = pendingQuery;
    final String cat = pendingCategory;
    final String status = statusFilter.getValue();

    final int pageToLoad = currentPage;
    final int sizeToLoad = pageSize;

    AsyncTask.<PageResponse<AuctionResponse>>run(() ->
            SessionManager.getApiClient().searchAuctionsPaged(q, status, cat, pageToLoad, sizeToLoad))
        .onSuccess(page -> {
          setLoading(false);
          results = page != null ? page.content() : null;
          totalPages = page != null ? page.totalPages() : 1;
          totalElements = page != null ? page.totalElements() : 0;
          pageInfoLabel.setText(String.format("Page %d of %d — %d items", currentPage + 1, totalPages, totalElements));
          prevPageButton.setDisable(currentPage <= 0);
          nextPageButton.setDisable(currentPage + 1 >= totalPages);
          auctionsContainer.getChildren().clear();
          if (results == null || results.isEmpty()) {
            emptyPane.setVisible(true);
            emptyPane.setManaged(true);
          } else {
            emptyPane.setVisible(false);
            emptyPane.setManaged(false);
            for (AuctionResponse a : results) {
              auctionsContainer.getChildren().add(AuctionCardFactory.createAuctionCard(a));
            }
          }
        })
        .onFailure(ex -> {
          setLoading(false);
          errorLabel.setText("Search failed: " + (ex != null ? ex.getMessage() : "Unknown"));
          errorLabel.setVisible(true);
          errorLabel.setManaged(true);
        })
        .start();
  }

  private void setLoading(boolean l) {
    loadingPane.setVisible(l);
    loadingPane.setManaged(l);
  }
}
