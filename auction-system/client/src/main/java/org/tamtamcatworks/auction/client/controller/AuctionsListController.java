package org.tamtamcatworks.auction.client.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
  @FXML private VBox auctionsContainer;

  @FXML
  public void initialize() {
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
      List<AuctionResponse> auctions = task.getValue();
      if (auctions == null || auctions.isEmpty()) {
        emptyPane.setVisible(true);
        emptyPane.setManaged(true);
      } else {
        for (AuctionResponse auction : auctions) {
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

  private HBox createAuctionCard(AuctionResponse auction) {
    HBox card = new HBox(16);
    card.setAlignment(Pos.CENTER_LEFT);
    card.getStyleClass().add("auction-card");

    // Left: Title + Seller
    VBox info = new VBox(4);
    info.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(info, Priority.ALWAYS);

    Label titleLabel = new Label(auction.title());
    titleLabel.getStyleClass().add("auction-title");

    Label sellerLabel = new Label("by " + auction.sellerName());
    sellerLabel.getStyleClass().add("auction-seller");

    // Item name
    Label itemLabel = new Label(auction.itemName() != null
        ? auction.itemName() : "No item");
    itemLabel.getStyleClass().add("text-muted");

    info.getChildren().addAll(titleLabel, sellerLabel, itemLabel);

    // Center: Status badge
    Label statusBadge = new Label(auction.status());
    statusBadge.getStyleClass().addAll("status-badge", getStatusClass(auction.status()));

    // Time info
    VBox timeBox = new VBox(2);
    timeBox.setAlignment(Pos.CENTER_RIGHT);
    timeBox.setMinWidth(140);

    String timeText = formatTimeInfo(auction);
    Label timeLabel = new Label(timeText);
    timeLabel.getStyleClass().add("auction-time");
    timeBox.getChildren().add(timeLabel);

    // Right: Price + View button
    VBox priceBox = new VBox(6);
    priceBox.setAlignment(Pos.CENTER_RIGHT);
    priceBox.setMinWidth(120);

    Label priceLabel = new Label(String.format("\\$%.2f", auction.currentPrice()));
    priceLabel.getStyleClass().add("auction-price");

    Label priceMeta = new Label("Current price");
    priceMeta.getStyleClass().add("auction-seller");

    Button viewBtn = new Button("View Details");
    viewBtn.getStyleClass().addAll("btn-secondary");
    viewBtn.setOnAction(e -> {
      Navigation.setContextData(auction.id());
      Navigation.navigateTo("/fxml/auction-detail.fxml");
    });

    priceBox.getChildren().addAll(priceLabel, priceMeta, viewBtn);

    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.SOMETIMES);

    card.getChildren().addAll(info, statusBadge, timeBox, spacer, priceBox);
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
}
