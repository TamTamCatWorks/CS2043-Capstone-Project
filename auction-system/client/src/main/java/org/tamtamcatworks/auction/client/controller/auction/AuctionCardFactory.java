package org.tamtamcatworks.auction.client.controller.auction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

public final class AuctionCardFactory {

  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

  private AuctionCardFactory() {}

  public static VBox createAuctionCard(AuctionResponse auction) {
    VBox card = new VBox(0);
    card.getStyleClass().add("asset-card");
    card.setPrefWidth(220);
    card.setMinWidth(220);
    card.setMaxWidth(220);

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

  private static String getStatusClass(String status) {
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

  private static String formatTimeInfo(AuctionResponse auction) {
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