package org.tamtamcatworks.auction.client.controller.shell;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

public class AuctionListCell extends ListCell<AuctionResponse> {
  @Override
  protected void updateItem(AuctionResponse item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setGraphic(null);
      setText(null);
      return;
    }
    HBox row = new HBox(12);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(10, 14, 10, 14));
    row.getStyleClass().add("profile-list-row");

    Label statusDot = new Label("●");
    statusDot
        .getStyleClass()
        .addAll("profile-status-dot", "status-dot-" + item.status().toLowerCase());

    VBox textBox = new VBox(2);
    HBox.setHgrow(textBox, Priority.ALWAYS);
    Label title = new Label(item.title());
    title.getStyleClass().add("profile-row-title");
    Label subtitle = new Label(item.itemName() + " · " + item.status());
    subtitle.getStyleClass().add("profile-row-subtitle");
    textBox.getChildren().addAll(title, subtitle);

    Label price = new Label(String.format("$%,.2f", item.currentPrice()));
    price.getStyleClass().add("profile-row-price");

    row.getChildren().addAll(statusDot, textBox, price);
    setGraphic(row);
  }
}
