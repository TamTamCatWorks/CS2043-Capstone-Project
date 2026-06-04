package org.tamtamcatworks.auction.client.controller.auction;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.springframework.messaging.simp.stomp.StompSession;
import org.tamtamcatworks.auction.shared.request.AutoBidRequest;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.AutoBidResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

/** Controller for the auction detail view with bidding. */
@Route(fxml = "/fxml/auction-detail.fxml", layout = Route.DASHBOARD_LAYOUT)
public class AuctionDetailController extends BaseController {

  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

  @FXML private Hyperlink backLink;
  @FXML private StackPane loadingPane;
  @FXML private Label errorLabel;
  @FXML private HBox contentPane;

  // Info panel
  @FXML private Label titleLabel;
  @FXML private Label statusLabel;
  @FXML private Label sellerLabel;
  @FXML private Label itemLabel;
  @FXML private Label startTimeLabel;
  @FXML private Label endTimeLabel;
  @FXML private Label startPriceLabel;
  @FXML private Label leadingBidderLabel;

  // Image components
  @FXML private ImageView itemImageView;
  @FXML private Label imagePlaceholderLabel;

  // Specific category details
  @FXML private Separator specificInfoSeparator;
  @FXML private VBox specificInfoBox;
  @FXML private Label specificInfoLabel;

  // Owner controls
  @FXML private HBox ownerControls;
  @FXML private Button openBtn;
  @FXML private Button closeBtn;

  // Bidding panel
  @FXML private Label currentPriceLabel;
  @FXML private VBox bidTrendBox;
  @FXML private LineChart<Number, Number> bidHistoryChart;
  @FXML private NumberAxis bidHistoryXAxis;
  @FXML private NumberAxis bidHistoryYAxis;
  @FXML private VBox bidFormBox;
  @FXML private TextField bidAmountField;
  @FXML private Label bidMessageLabel;
  @FXML private Button placeBidBtn;
  @FXML private VBox autoBidFormBox;
  @FXML private Label autoBidIncrementLabel;
  @FXML private Label autoBidStateLabel;
  @FXML private Label autoBidMaxLabel;
  @FXML private TextField autoBidMaxField;
  @FXML private Label autoBidMessageLabel;
  @FXML private Button autoBidBtn;
  @FXML private Button cancelAutoBidBtn;
  @FXML private VBox bidHistoryContainer;
  @FXML private Label noBidsLabel;

  private String auctionId;
  private AuctionResponse currentAuction;

  private StompSession.Subscription priceSubscription;
  private StompSession.Subscription statusSubscription;

  @FXML
  public void initialize() {
    auctionId = Navigation.getContextData();
    if (auctionId == null || auctionId.isEmpty()) {
      showError(errorLabel, "No auction ID provided.");
      return;
    }
    loadAuctionDetail();
    setupWebSocket();
  }

  private void setupWebSocket() {
    SessionManager.subscribeToPrice(auctionId, priceUpdate -> {
      javafx.application.Platform.runLater(() -> {
        currentPriceLabel.setText(String.format("$%.2f", priceUpdate.newPrice()));
        if (currentAuction != null) {
          currentAuction = new AuctionResponse(
              currentAuction.id(),
              currentAuction.title(),
              currentAuction.sellerId(),
              currentAuction.sellerName(),
              currentAuction.itemId(),
              currentAuction.itemName(),
              currentAuction.leadingBidderId(),
              currentAuction.leadingBidderName(),
              currentAuction.startingPrice(),
              priceUpdate.newPrice(),
              currentAuction.minimumIncrement(),
              currentAuction.status(),
              currentAuction.startTime(),
              currentAuction.endTime(),
              currentAuction.imageUrl(),
              currentAuction.itemDescription(),
              currentAuction.itemType(),
              currentAuction.specificInfo()
          );
        }
        loadAuctionDetail();
      });
    }).thenAccept(sub -> priceSubscription = sub)
      .exceptionally(ex -> { System.err.println("WebSocket subscribe failed: " + (ex != null ? ex.getMessage() : "?")); return null; });

    SessionManager.subscribeToStatus(auctionId, statusUpdate -> {
      javafx.application.Platform.runLater(this::loadAuctionDetail);
    }).thenAccept(sub -> statusSubscription = sub)
      .exceptionally(ex -> { System.err.println("WebSocket subscribe failed: " + (ex != null ? ex.getMessage() : "?")); return null; });

    if (contentPane != null) {
      contentPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
        if (newScene == null) {
          cleanupWebSocket();
        }
      });
    }
  }

  private void cleanupWebSocket() {
    if (priceSubscription != null) {
      SessionManager.unsubscribe(priceSubscription);
      priceSubscription = null;
    }
    if (statusSubscription != null) {
      SessionManager.unsubscribe(statusSubscription);
      statusSubscription = null;
    }
  }

  private void loadAuctionDetail() {
    setLoading(true);
    hideError();

    AsyncTask.<AuctionResponse>run(() -> api.getAuction(auctionId))
        .onSuccess(auction -> {
          setLoading(false);
          currentAuction = auction;
          populateAuctionInfo(currentAuction);
          loadBidHistory();
          loadAutoBidState();
        })
        .onFailure(ex -> {
          setLoading(false);
          showError(errorLabel, "Failed to load auction: "
              + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
        })
        .start();
  }

  private void populateAuctionInfo(AuctionResponse auction) {
    contentPane.setVisible(true);
    contentPane.setManaged(true);

    // Load item image asynchronously
    if (auction.imageUrl() != null && !auction.imageUrl().isEmpty()) {
      try {
        Image img = new Image(auction.imageUrl(), true); // backgroundLoading = true
        itemImageView.setImage(img);
        imagePlaceholderLabel.setVisible(false);
        imagePlaceholderLabel.setManaged(false);
        itemImageView.setVisible(true);
        itemImageView.setManaged(true);
      } catch (Exception e) {
        itemImageView.setVisible(false);
        itemImageView.setManaged(false);
        imagePlaceholderLabel.setVisible(true);
        imagePlaceholderLabel.setManaged(true);
      }
    } else {
      itemImageView.setVisible(false);
      itemImageView.setManaged(false);
      imagePlaceholderLabel.setVisible(true);
      imagePlaceholderLabel.setManaged(true);
    }

    titleLabel.setText(auction.title());
    sellerLabel.setText(auction.sellerName() != null ? auction.sellerName() : "-");
    itemLabel.setText(auction.itemName() != null ? auction.itemName() : "-");
    startPriceLabel.setText(formatMoney(auction.startingPrice()));
    autoBidIncrementLabel.setText(formatMoney(auction.minimumIncrement()));
    currentPriceLabel.setText(formatMoney(auction.currentPrice()));

    leadingBidderLabel.setText(
        auction.leadingBidderName() != null ? auction.leadingBidderName() : "No bids yet");

    if (auction.startTime() != null) {
      startTimeLabel.setText(auction.startTime().format(TIME_FMT));
    }
    if (auction.endTime() != null) {
      endTimeLabel.setText(auction.endTime().format(TIME_FMT));
    }

    // Specific category details
    if (auction.specificInfo() != null && !auction.specificInfo().trim().isEmpty()) {
      specificInfoLabel.setText(auction.specificInfo().trim());
      specificInfoSeparator.setVisible(true);
      specificInfoSeparator.setManaged(true);
      specificInfoBox.setVisible(true);
      specificInfoBox.setManaged(true);
    } else {
      specificInfoSeparator.setVisible(false);
      specificInfoSeparator.setManaged(false);
      specificInfoBox.setVisible(false);
      specificInfoBox.setManaged(false);
    }

    // Status badge
    String status = auction.status() != null ? auction.status() : "PENDING";
    statusLabel.setText(status);
    statusLabel.getStyleClass().removeAll(
        "status-active", "status-pending", "status-closed", "status-cancelled");
    statusLabel.getStyleClass().add(getStatusClass(status));

    // Show bid form only for active auctions the user doesn't own
    String currentUserId = SessionManager.getCurrentUser() != null
        ? SessionManager.getCurrentUser().id() : null;
    boolean isOwner = currentUserId != null && currentUserId.equals(auction.sellerId());
    boolean isActive = "ACTIVE".equalsIgnoreCase(status);

    if (isActive && !isOwner) {
      bidFormBox.setVisible(true);
      bidFormBox.setManaged(true);
      autoBidFormBox.setVisible(true);
      autoBidFormBox.setManaged(true);
    } else {
      bidFormBox.setVisible(false);
      bidFormBox.setManaged(false);
      autoBidFormBox.setVisible(false);
      autoBidFormBox.setManaged(false);
    }

    // Show owner controls
    if (isOwner) {
      ownerControls.setVisible(true);
      ownerControls.setManaged(true);
      openBtn.setVisible("PENDING".equalsIgnoreCase(status));
      openBtn.setManaged("PENDING".equalsIgnoreCase(status));
      closeBtn.setVisible("ACTIVE".equalsIgnoreCase(status));
      closeBtn.setManaged("ACTIVE".equalsIgnoreCase(status));
    } else {
      ownerControls.setVisible(false);
      ownerControls.setManaged(false);
    }
  }

  private void loadBidHistory() {
    AsyncTask.<List<BidResponse>>run(() -> api.getBids(auctionId))
        .onSuccess(bids -> {
          bidHistoryContainer.getChildren().clear();
          if (bids == null || bids.isEmpty()) {
            clearBidChart();
            noBidsLabel.setVisible(true);
            noBidsLabel.setManaged(true);
          } else {
            noBidsLabel.setVisible(false);
            noBidsLabel.setManaged(false);
            populateBidChart(bids);
            for (BidResponse bid : bids) {
              bidHistoryContainer.getChildren().add(createBidRow(bid));
            }
          }
        })
        .onFailure(ex -> {
          noBidsLabel.setText("Could not load bid history");
          noBidsLabel.setVisible(true);
          noBidsLabel.setManaged(true);
        })
        .start();
  }

  private void loadAutoBidState() {
    if (currentAuction == null || currentAuction.sellerId() == null) {
      clearAutoBidState();
      return;
    }

    String currentUserId = SessionManager.getCurrentUser() != null
        ? SessionManager.getCurrentUser().id() : null;
    boolean canManageAutoBid = currentUserId != null && !currentUserId.equals(currentAuction.sellerId())
        && "ACTIVE".equalsIgnoreCase(currentAuction.status());
    if (!canManageAutoBid) {
      clearAutoBidState();
      return;
    }

    AsyncTask.<AutoBidResponse>run(() -> api.getAutoBid(auctionId))
        .onSuccess(autoBid -> {
          showAutoBidState(autoBid);
        })
        .onFailure(ex -> clearAutoBidState())
        .start();
  }

  private void showAutoBidState(AutoBidResponse autoBid) {
    if (autoBid == null) {
      clearAutoBidState();
      return;
    }

    autoBidStateLabel.setText(autoBid.active() ? "Active auto-bid" : "Inactive auto-bid");
    autoBidMaxLabel.setText(formatMoney(autoBid.maxBid()));
    autoBidMaxField.setText(String.format("%.2f", autoBid.maxBid()));
    autoBidBtn.setText("Update Auto-Bid");
    cancelAutoBidBtn.setVisible(true);
    cancelAutoBidBtn.setManaged(true);
    autoBidStateLabel.setVisible(true);
    autoBidStateLabel.setManaged(true);
    autoBidMaxLabel.setVisible(true);
    autoBidMaxLabel.setManaged(true);
  }

  private void clearAutoBidState() {
    autoBidStateLabel.setText("No auto-bid set");
    autoBidMaxLabel.setText("-");
    autoBidBtn.setText("Place Auto-Bid");
    cancelAutoBidBtn.setVisible(false);
    cancelAutoBidBtn.setManaged(false);
    autoBidStateLabel.setVisible(true);
    autoBidStateLabel.setManaged(true);
    autoBidMaxLabel.setVisible(true);
    autoBidMaxLabel.setManaged(true);
  }

  private void populateBidChart(List<BidResponse> bids) {
    if (bidHistoryChart == null) {
      return;
    }

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName("Bid amount");

    int bidIndex = 1;
    double highestBid = Double.NEGATIVE_INFINITY;
    for (BidResponse bid : bids) {
      double amount = bid.amount();
      series.getData().add(new XYChart.Data<>(bidIndex++, amount));
      highestBid = Math.max(highestBid, amount);
    }

    if (bidTrendBox != null) {
      bidTrendBox.setVisible(true);
      bidTrendBox.setManaged(true);
    }
    bidHistoryChart.getData().clear();
    bidHistoryChart.getData().add(series);
    bidHistoryChart.setVisible(true);
    bidHistoryChart.setManaged(true);

    if (bidHistoryXAxis != null) {
      bidHistoryXAxis.setAutoRanging(false);
      bidHistoryXAxis.setLowerBound(1);
      bidHistoryXAxis.setUpperBound(Math.max(2, bids.size()));
      bidHistoryXAxis.setTickUnit(1);
      bidHistoryXAxis.setLabel("Bid #");
    }

    if (bidHistoryYAxis != null) {
      bidHistoryYAxis.setAutoRanging(false);
      double upperBound = highestBid == Double.NEGATIVE_INFINITY ? 1.0 : highestBid;
      bidHistoryYAxis.setLowerBound(0);
      bidHistoryYAxis.setUpperBound(Math.max(1.0, upperBound * 1.1));
      bidHistoryYAxis.setTickUnit(Math.max(1.0, upperBound / 4.0));
      bidHistoryYAxis.setLabel("Amount ($)");
    }
  }

  private void clearBidChart() {
    if (bidHistoryChart == null) {
      return;
    }

    bidHistoryChart.getData().clear();
    if (bidTrendBox != null) {
      bidTrendBox.setVisible(false);
      bidTrendBox.setManaged(false);
    }
  }

  private HBox createBidRow(BidResponse bid) {
    HBox row = new HBox(12);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("bid-history-row");

    Label bidderLabel = new Label(bid.bidderName() != null ? bid.bidderName() : "Anonymous");
    bidderLabel.getStyleClass().add("bid-bidder");
    HBox.setHgrow(bidderLabel, Priority.ALWAYS);

    Label amountLabel = new Label(String.format("$%.2f", bid.amount()));
    amountLabel.getStyleClass().add("bid-amount");

    Label timeLabel = new Label(
        bid.createdAt() != null ? bid.createdAt().format(TIME_FMT) : "");
    timeLabel.getStyleClass().add("bid-time");

    row.getChildren().addAll(bidderLabel, amountLabel, timeLabel);
    return row;
  }

  @FXML
  private void handlePlaceBid() {
    String text = bidAmountField.getText().trim();
    if (text.isEmpty()) {
      showBidMessage("Please enter a bid amount.", true);
      return;
    }

    double amount;
    try {
      amount = Double.parseDouble(text);
    } catch (NumberFormatException e) {
      showBidMessage("Please enter a valid number.", true);
      return;
    }

    if (currentAuction != null && amount <= currentAuction.currentPrice()) {
      showBidMessage(String.format("Bid must be higher than $%.2f", currentAuction.currentPrice()),
          true);
      return;
    }

    placeBidBtn.setDisable(true);
    BidRequest request = new BidRequest(amount, "MANUAL");

    AsyncTask.<UserResponse>run(() -> {
          api.placeBid(auctionId, request);
          return null;
        })
        .onSuccess(refreshedUser -> {
          placeBidBtn.setDisable(false);
          bidAmountField.clear();
          showBidMessage("Bid placed successfully!", false);
          // Refresh the auction detail to show updated price and bid history
          loadAuctionDetail();
        })
        .onFailure(ex -> {
          placeBidBtn.setDisable(false);
          showBidMessage("Bid failed: "
              + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"), true);
        })
        .start();
  }

  @FXML
  private void handlePlaceAutoBid() {
    String text = autoBidMaxField.getText().trim();
    if (text.isEmpty()) {
      showAutoBidMessage("Please enter a maximum bid.", true);
      return;
    }

    double maxBid;
    try {
      maxBid = Double.parseDouble(text);
    } catch (NumberFormatException e) {
      showAutoBidMessage("Please enter a valid number.", true);
      return;
    }

    if (currentAuction != null && maxBid <= currentAuction.currentPrice()) {
      showAutoBidMessage(String.format("Maximum bid must be higher than $%.2f",
          currentAuction.currentPrice()), true);
      return;
    }

    autoBidBtn.setDisable(true);
    AutoBidRequest request = new AutoBidRequest(maxBid);

    AsyncTask.<AutoBidResponse>run(() -> api.registerAutoBid(auctionId, request))
        .onSuccess(response -> {
          autoBidBtn.setDisable(false);
          autoBidMaxField.clear();
          showAutoBidMessage("Auto-bid placed successfully!", false);
          loadAuctionDetail();
        })
        .onFailure(ex -> {
          autoBidBtn.setDisable(false);
          showAutoBidMessage("Auto-bid failed: "
              + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"), true);
        })
        .start();
  }

  @FXML
  private void handleCancelAutoBid() {
    cancelAutoBidBtn.setDisable(true);

    AsyncTask.<Void>run(() -> {
          api.cancelAutoBid(auctionId);
          return null;
        })
        .onSuccess(ignored -> {
          cancelAutoBidBtn.setDisable(false);
          autoBidMaxField.clear();
          showAutoBidMessage("Auto-bid canceled successfully.", false);
          clearAutoBidState();
          loadAuctionDetail();
        })
        .onFailure(ex -> {
          cancelAutoBidBtn.setDisable(false);
          showAutoBidMessage("Failed to cancel auto-bid: "
              + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"), true);
        })
        .start();
  }

  @FXML
  private void handleOpenAuction() {
    runAuctionAction(() -> api.openAuction(auctionId));
  }

  @FXML
  private void handleCloseAuction() {
    runAuctionAction(() -> api.closeAuction(auctionId));
  }

  private void runAuctionAction(java.util.concurrent.Callable<AuctionResponse> action) {
    AsyncTask.<AuctionResponse>run(action)
        .onSuccess(res -> loadAuctionDetail())
        .onFailure(ex -> {
          showError(errorLabel, "Action failed: "
              + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
        })
        .start();
  }

  @FXML
  private void handleBack() {
    Navigation.navigateTo("/fxml/auctions-list.fxml");
  }

  private void showBidMessage(String msg, boolean isError) {
    bidMessageLabel.setText(msg);
    bidMessageLabel.setVisible(true);
    bidMessageLabel.setManaged(true);
    bidMessageLabel.getStyleClass().removeAll("error-label", "success-label");
    bidMessageLabel.getStyleClass().add(isError ? "error-label" : "success-label");
  }

  private void showAutoBidMessage(String msg, boolean isError) {
    autoBidMessageLabel.setText(msg);
    autoBidMessageLabel.setVisible(true);
    autoBidMessageLabel.setManaged(true);
    autoBidMessageLabel.getStyleClass().removeAll("error-label", "success-label");
    autoBidMessageLabel.getStyleClass().add(isError ? "error-label" : "success-label");
  }

  private String getStatusClass(String status) {
    return switch (status.toUpperCase()) {
      case "ACTIVE" -> "status-active";
      case "PENDING" -> "status-pending";
      case "CLOSED" -> "status-closed";
      case "CANCELLED" -> "status-cancelled";
      default -> "status-pending";
    };
  }

  private String formatMoney(double amount) {
    return String.format("$%.2f", amount);
  }

  private void hideError() {
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
  }

  private void setLoading(boolean loading) {
    loadingPane.setVisible(loading);
    loadingPane.setManaged(loading);
  }
}
