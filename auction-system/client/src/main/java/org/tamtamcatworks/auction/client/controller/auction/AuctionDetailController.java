package org.tamtamcatworks.auction.client.controller.auction;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

/** Controller for the auction detail view with bidding. */
public class AuctionDetailController {

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
  @FXML private VBox bidFormBox;
  @FXML private TextField bidAmountField;
  @FXML private Label bidMessageLabel;
  @FXML private Button placeBidBtn;
  @FXML private VBox bidHistoryContainer;
  @FXML private Label noBidsLabel;

  private String auctionId;
  private AuctionResponse currentAuction;

  @FXML
  public void initialize() {
    auctionId = Navigation.getContextData();
    if (auctionId == null || auctionId.isEmpty()) {
      showError("No auction ID provided.");
      return;
    }
    loadAuctionDetail();
  }

  private void loadAuctionDetail() {
    setLoading(true);
    hideError();

    Task<AuctionResponse> task = new Task<>() {
      @Override
      protected AuctionResponse call() throws Exception {
        return SessionManager.getApiClient().getAuction(auctionId);
      }
    };

    task.setOnSucceeded(e -> {
      setLoading(false);
      currentAuction = task.getValue();
      populateAuctionInfo(currentAuction);
      loadBidHistory();
    });

    task.setOnFailed(e -> {
      setLoading(false);
      Throwable ex = task.getException();
      showError("Failed to load auction: "
          + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
    });

    new Thread(task).start();
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
    startPriceLabel.setText(String.format("$%.2f", auction.startingPrice()));
    currentPriceLabel.setText(String.format("$%.2f", auction.currentPrice()));

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
    }

    // Show owner controls
    if (isOwner) {
      ownerControls.setVisible(true);
      ownerControls.setManaged(true);
      openBtn.setVisible("PENDING".equalsIgnoreCase(status));
      openBtn.setManaged("PENDING".equalsIgnoreCase(status));
      closeBtn.setVisible("ACTIVE".equalsIgnoreCase(status));
      closeBtn.setManaged("ACTIVE".equalsIgnoreCase(status));
    }
  }

  private void loadBidHistory() {
    Task<List<BidResponse>> task = new Task<>() {
      @Override
      protected List<BidResponse> call() throws Exception {
        return SessionManager.getApiClient().getBids(auctionId);
      }
    };

    task.setOnSucceeded(e -> {
      List<BidResponse> bids = task.getValue();
      bidHistoryContainer.getChildren().clear();
      if (bids == null || bids.isEmpty()) {
        noBidsLabel.setVisible(true);
        noBidsLabel.setManaged(true);
      } else {
        noBidsLabel.setVisible(false);
        noBidsLabel.setManaged(false);
        for (BidResponse bid : bids) {
          bidHistoryContainer.getChildren().add(createBidRow(bid));
        }
      }
    });

    task.setOnFailed(e -> {
      // Silently handle bid history load failure
      noBidsLabel.setText("Could not load bid history");
      noBidsLabel.setVisible(true);
      noBidsLabel.setManaged(true);
    });

    new Thread(task).start();
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

    Task<UserResponse> task = new Task<>() {
      @Override
      protected UserResponse call() throws Exception {
        SessionManager.getApiClient().placeBid(auctionId, request);
        if (SessionManager.getCurrentUser() == null) {
          return null;
        }
        return SessionManager.getApiClient().getUser(SessionManager.getCurrentUser().id());
      }
    };

    task.setOnSucceeded(e -> {
      placeBidBtn.setDisable(false);
      UserResponse refreshedUser = task.getValue();
      if (refreshedUser != null) {
        SessionManager.setCurrentUser(refreshedUser);
      }
      bidAmountField.clear();
      showBidMessage("Bid placed successfully!", false);
      // Refresh the auction detail to show updated price and bid history
      loadAuctionDetail();
    });

    task.setOnFailed(e -> {
      placeBidBtn.setDisable(false);
      Throwable ex = task.getException();
      showBidMessage("Bid failed: "
          + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"), true);
    });

    new Thread(task).start();
  }

  @FXML
  private void handleOpenAuction() {
    runAuctionAction(() -> SessionManager.getApiClient().openAuction(auctionId));
  }

  @FXML
  private void handleCloseAuction() {
    runAuctionAction(() -> SessionManager.getApiClient().closeAuction(auctionId));
  }

  private void runAuctionAction(java.util.concurrent.Callable<AuctionResponse> action) {
    Task<AuctionResponse> task = new Task<>() {
      @Override
      protected AuctionResponse call() throws Exception {
        return action.call();
      }
    };

    task.setOnSucceeded(e -> loadAuctionDetail());
    task.setOnFailed(e -> {
      Throwable ex = task.getException();
      showError("Action failed: "
          + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
    });

    new Thread(task).start();
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

  private String getStatusClass(String status) {
    return switch (status.toUpperCase()) {
      case "ACTIVE" -> "status-active";
      case "PENDING" -> "status-pending";
      case "CLOSED" -> "status-closed";
      case "CANCELLED" -> "status-cancelled";
      default -> "status-pending";
    };
  }

  private void showError(String msg) {
    errorLabel.setText(msg);
    errorLabel.setVisible(true);
    errorLabel.setManaged(true);
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
