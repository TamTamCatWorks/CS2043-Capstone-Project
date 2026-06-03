package org.tamtamcatworks.auction.client.util.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public final class UiStateHelper {

    private UiStateHelper() {}

    public static void showError(
            StackPane container,
            String message
    ) {

        container.getChildren().setAll(
                new Label(message)
        );
    }

    public static void showLoading(
            StackPane container
    ) {

        container.getChildren().setAll(
                new Label("Loading...")
        );
    }
}