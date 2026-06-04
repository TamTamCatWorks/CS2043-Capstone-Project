package org.tamtamcatworks.auction.client.controller.shell;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

public class NotificationListCell extends ListCell<NotificationResponse> {
  @Override
  protected void updateItem(NotificationResponse item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setGraphic(null);
      setText(null);
      return;
    }
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    VBox v = new VBox(2);
    Label msg = new Label(item.message());
    msg.getStyleClass().add("profile-row-title");
    Label meta = new Label(item.createdAt());
    meta.getStyleClass().add("profile-row-subtitle");
    v.getChildren().addAll(msg, meta);
    if (!item.read()) {
      Label unread = new Label("●");
      unread.getStyleClass().add("notification-unread-dot");
      row.getChildren().addAll(unread, v);
    } else {
      row.getChildren().add(v);
    }
    setGraphic(row);
  }
}
