package org.tamtamcatworks.auction.client.controller.auction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
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

/** Controller for the auction browse/list view. */
public class AuctionsListController {

  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

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
    String pendingQuery = SessionManager.getPendingSearchQuery();
    String pendingCategory = SessionManager.getPendingSearchCategory();
    if (pendingQuery != null) {
      searchQuery = pendingQuery.trim();
    }
    if (pendingCategory != null && !pendingCategory.isBlank()) {
      searchCategory = pendingCategory.trim();
    }
    if (pendingQuery != null || pendingCategory != null) {
      SessionManager.clearPendingSearch();
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

    Task<List<AuctionResponse>> task = new Task<>() {
      @Override
      protected List<AuctionResponse> call() throws Exception {
        if ("ALL".equals(selected)) {
          return SessionManager.getApiClient().getAllAuctions();
        } else {
          return SessionManager.getApiClient().getAuctionsByStatus(selected);
        }
      }
    };

    task.setOnSucceeded(e -> {
      setLoading(false);
      loadedAuctions.clear();
      List<AuctionResponse> auctions = task.getValue();
      if (auctions != null) {
        loadedAuctions.addAll(auctions);
      }

      List<AuctionResponse> filteredAuctions = applySearchFilter(loadedAuctions);
      if (filteredAuctions.isEmpty()) {
        emptyPane.setVisible(true);
        emptyPane.setManaged(true);
      } else {
        for (AuctionResponse auction : filteredAuctions) {
          auctionsContainer.getChildren().add(createAuctionCard(auction));
        }
      }
    });

    task.setOnFailed(e -> {
      setLoading(false);
      Throwable ex = task.getException();
      String msg = ex != null && ex.getMessage() != null
          ? ex.getMessage() : "Unknown error";
      errorLabel.setText("Failed to load auctions: " + msg);
      errorLabel.setVisible(true);
      errorLabel.setManaged(true);
    });

    new Thread(task).start();
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
        Image img = new Image(auction.imageUrl(), true); // backgroundLoading = true
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

    // Category / Item Type Badge
    String itemType = auction.itemType() != null ? auction.itemType() : "Unknown";
    Label categoryBadge = new Label(itemType.toUpperCase());
    categoryBadge.getStyleClass().addAll("category-badge", "category-" + itemType.toLowerCase());

    // Title
    Label titleLabel = new Label(auction.title());
    titleLabel.getStyleClass().add("asset-card-title");
    titleLabel.setWrapText(true);
    titleLabel.setMaxHeight(40);
    titleLabel.setMinHeight(40);

    // Seller Info
    Label sellerLabel = new Label("by " + (auction.sellerName() != null ? auction.sellerName() : "Unknown"));
    sellerLabel.getStyleClass().add("asset-card-seller");

    // Price + Status Row
    HBox priceStatusRow = new HBox(8);
    priceStatusRow.setAlignment(Pos.CENTER_LEFT);

    Label priceLabel = new Label(String.format("$%.2f", auction.currentPrice()));
    priceLabel.getStyleClass().add("asset-card-price");

    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Label statusBadge = new Label(auction.status());
    statusBadge.getStyleClass().addAll("status-badge", getStatusClass(auction.status()));
    
    priceStatusRow.getChildren().addAll(priceLabel, spacer, statusBadge);

    // Date/Time Row
    String timeText = formatTimeInfo(auction);
    Label timeLabel = new Label(timeText);
    timeLabel.getStyleClass().add("auction-time");

    details.getChildren().addAll(categoryBadge, titleLabel, sellerLabel, priceStatusRow, timeLabel);

    card.getChildren().addAll(imgWrapper, details);

    // Click handler for details view navigation
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

  private void setLoading(boolean loading) {
    loadingPane.setVisible(loading);
    loadingPane.setManaged(loading);
  }

  private List<AuctionResponse> applySearchFilter(List<AuctionResponse> auctions) {
    if ((searchQuery == null || searchQuery.isBlank())
        && (searchCategory == null || searchCategory.isBlank()
            || "All categories".equalsIgnoreCase(searchCategory))) {
      return auctions;
    }

    String normalized = searchQuery == null ? "" : searchQuery.toLowerCase();
    String categoryNormalized = searchCategory == null ? "" : searchCategory.toLowerCase();
    return auctions.stream()
        .filter(auction -> matchesQuery(auction, normalized, categoryNormalized))
        .toList();
  }

  private boolean matchesQuery(AuctionResponse auction, String normalizedQuery, String categoryNormalized) {
    boolean queryMatches = normalizedQuery.isBlank()
        || containsIgnoreCase(auction.title(), normalizedQuery)
        || containsIgnoreCase(auction.itemName(), normalizedQuery)
        || containsIgnoreCase(auction.itemDescription(), normalizedQuery)
        || containsIgnoreCase(auction.itemType(), normalizedQuery)
        || containsIgnoreCase(auction.sellerName(), normalizedQuery)
        || containsIgnoreCase(auction.specificInfo(), normalizedQuery);

    boolean categoryMatches = categoryNormalized.isBlank()
        || "all categories".equals(categoryNormalized)
        || containsIgnoreCase(auction.itemType(), categoryNormalized);

    return queryMatches && categoryMatches;
  }

  private boolean containsIgnoreCase(String value, String normalizedQuery) {
    return value != null && value.toLowerCase().contains(normalizedQuery);
  }
}
