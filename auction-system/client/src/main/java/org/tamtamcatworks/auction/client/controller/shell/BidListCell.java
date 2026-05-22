package org.tamtamcatworks.auction.client.controller.shell;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tamtamcatworks.auction.shared.response.BidResponse;

public class BidListCell extends ListCell<BidResponse> {
    @Override
    protected void updateItem(BidResponse item, boolean empty) {
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

        FontIcon icon = new FontIcon("mdi2c-cash");
        icon.setIconSize(18);
        icon.getStyleClass().add("profile-bid-icon");

        VBox textBox = new VBox(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label auctionLabel = new Label("Auction: " + item.auctionId().substring(0,
            Math.min(8, item.auctionId().length())) + "…");
        auctionLabel.getStyleClass().add("profile-row-title");
        Label timeLabel = new Label(item.createdAt() != null ? item.createdAt().toString() : "");
        timeLabel.getStyleClass().add("profile-row-subtitle");
        textBox.getChildren().addAll(auctionLabel, timeLabel);

        Label amount = new Label(String.format("$%,.2f", item.amount()));
        amount.getStyleClass().add("profile-row-price");

        row.getChildren().addAll(icon, textBox, amount);
        setGraphic(row);
    }
}
