package org.tamtamcatworks.auction.client.controller.auction;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.tamtamcatworks.auction.shared.response.PageResponse;

import java.time.LocalDateTime;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

public class SearchResultsController {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

  @FXML private ComboBox<String> statusFilter;
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
    statusFilter.setOnAction(e -> runSearch());

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
    String pendingQuery = SessionManager.getPendingSearchQuery();
    String pendingCategory = SessionManager.getPendingSearchCategory();
    if (pendingQuery == null) pendingQuery = "";
    if (pendingCategory == null) pendingCategory = "";

    subtitleLabel.setText("Results for \"" + pendingQuery + "\"");

    final String q = pendingQuery;
    final String cat = pendingCategory;
    final String status = statusFilter.getValue();

    final int pageToLoad = currentPage;
    final int sizeToLoad = pageSize;

    Task<org.tamtamcatworks.auction.shared.response.PageResponse<AuctionResponse>> task = new Task<>() {
      @Override
      protected org.tamtamcatworks.auction.shared.response.PageResponse<AuctionResponse> call() throws Exception {
        return SessionManager.getApiClient().searchAuctionsPaged(q, status, cat, pageToLoad, sizeToLoad);
      }
    };

    task.setOnSucceeded(e -> {
      setLoading(false);
      org.tamtamcatworks.auction.shared.response.PageResponse<AuctionResponse> page = task.getValue();
      results = page != null ? page.content() : null;
      totalPages = page != null ? page.totalPages() : 1;
      totalElements = page != null ? page.totalElements() : 0;
      // update page info
      pageInfoLabel.setText(String.format("Page %d of %d — %d items", currentPage + 1, totalPages, totalElements));
      prevPageButton.setDisable(currentPage <= 0);
      nextPageButton.setDisable(currentPage + 1 >= totalPages);
      auctionsContainer.getChildren().clear();
      if (results == null || results.isEmpty()) {
        emptyPane.setVisible(true);
        emptyPane.setManaged(true);
      } else {
        for (AuctionResponse a : results) {
          auctionsContainer.getChildren().add(createAuctionCard(a));
        }
      }
    });

    task.setOnFailed(e -> {
      setLoading(false);
      Throwable ex = task.getException();
      errorLabel.setText("Search failed: " + (ex != null ? ex.getMessage() : "Unknown"));
      errorLabel.setVisible(true);
      errorLabel.setManaged(true);
    });

    new Thread(task).start();
  }

  private void setLoading(boolean l) {
    loadingPane.setVisible(l);
    loadingPane.setManaged(l);
  }

  private VBox createAuctionCard(AuctionResponse auction) {
    VBox card = new VBox(0);
    card.getStyleClass().add("asset-card");
    card.setPrefWidth(220);
    card.setMinWidth(220);
    card.setMaxWidth(220);

    // Image Section
    StackPane imgWrapper = new StackPane();
    imgWrapper.getStyleClass().add("asset-card-image-wrapper");
    imgWrapper.setPrefHeight(140);
    imgWrapper.setMinHeight(140);
    imgWrapper.setMaxHeight(140);

    ImageView imgView = new ImageView();
    imgView.setFitWidth(220);
    imgView.setFitHeight(140);
    imgView.setPreserveRatio(true);

    Label placeholderLabel = new Label();
    placeholderLabel.getStyleClass().add("text-muted");
    placeholderLabel.setStyle("-fx-font-size: 9pt;");

    if (auction.imageUrl() != null && !auction.imageUrl().isEmpty()) {
      try {
        Image img = new Image(auction.imageUrl(), true);
        imgView.setImage(img);
        placeholderLabel.setVisible(false);
      } catch (Exception e) {
        placeholderLabel.setText("No Image");
        imgView.setVisible(false);
      }
    } else {
      placeholderLabel.setText("No Image");
      imgView.setVisible(false);
    }

    imgWrapper.getChildren().addAll(imgView, placeholderLabel);

    // Details Section
    VBox details = new VBox(6);
    details.getStyleClass().add("asset-card-details");

    String itemType = auction.itemType() != null ? auction.itemType() : "Unknown";
    Label categoryBadge = new Label(itemType.toUpperCase());
    categoryBadge.getStyleClass().addAll("category-badge", "category-" + itemType.toLowerCase());

    Label titleLabel = new Label(auction.title());
    titleLabel.getStyleClass().add("asset-card-title");
    titleLabel.setWrapText(true);
    titleLabel.setMaxHeight(40);
    titleLabel.setMinHeight(40);

    Label sellerLabel = new Label("by " + (auction.sellerName() != null ? auction.sellerName() : "Unknown"));
    sellerLabel.getStyleClass().add("asset-card-seller");

    HBox priceStatusRow = new HBox(8);
    priceStatusRow.setAlignment(Pos.CENTER_LEFT);

    Label priceLabel = new Label(String.format("$%.2f", auction.currentPrice()));
    priceLabel.getStyleClass().add("asset-card-price");

    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Label statusBadge = new Label(auction.status());
    statusBadge.getStyleClass().addAll("status-badge", getStatusClass(auction.status()));

    priceStatusRow.getChildren().addAll(priceLabel, spacer, statusBadge);

    String timeText = formatTimeInfo(auction);
    Label timeLabel = new Label(timeText);
    timeLabel.getStyleClass().add("auction-time");

    details.getChildren().addAll(categoryBadge, titleLabel, sellerLabel, priceStatusRow, timeLabel);

    card.getChildren().addAll(imgWrapper, details);

    card.setOnMouseClicked(e -> {
      Navigation.setContextData(auction.id());
      Navigation.navigateTo("/fxml/auction-detail.fxml");
    });

    return card;
  }

  private String getStatusClass(String status) {
    if (status == null) {
      return "status-pending";
    }
    return switch (status.toUpperCase()) {
      case "ACTIVE" -> "status-active";
      case "PENDING" -> "status-pending";
      case "CLOSED" -> "status-closed";
      case "CANCELLED" -> "status-cancelled";
      default -> "status-pending";
    };
  }

  private String formatTimeInfo(AuctionResponse auction) {
    LocalDateTime now = LocalDateTime.now();
    if (auction.endTime() != null && auction.endTime().isBefore(now)) {
      return "Ended " + auction.endTime().format(TIME_FMT);
    } else if (auction.startTime() != null && auction.startTime().isAfter(now)) {
      return "Starts " + auction.startTime().format(TIME_FMT);
    } else if (auction.endTime() != null) {
      return "Ends " + auction.endTime().format(TIME_FMT);
    }
    return "";
  }
}
