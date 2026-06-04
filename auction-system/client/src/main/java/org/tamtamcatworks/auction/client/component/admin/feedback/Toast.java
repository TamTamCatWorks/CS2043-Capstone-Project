package org.tamtamcatworks.auction.client.component.admin.feedback;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class Toast {

  private Toast() {}

  public static void show(String message, ToastType type) {

    Popup popup = new Popup();

    Label label = new Label(message);

    label
        .getStyleClass()
        .addAll(
            "toast",
            switch (type) {
              case SUCCESS -> "toast-success";

              case ERROR -> "toast-error";

              case INFO -> "toast-info";
            });

    StackPane root = new StackPane(label);

    root.setAlignment(Pos.CENTER);

    popup.getContent().add(root);

    Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).getFirst();

    Scene scene = stage.getScene();

    popup.show(stage);

    popup.setX(stage.getX() + scene.getWidth() - 350);

    popup.setY(stage.getY() + 80);

    PauseTransition delay = new PauseTransition(Duration.seconds(3));

    delay.setOnFinished(event -> popup.hide());

    delay.play();
  }
}
