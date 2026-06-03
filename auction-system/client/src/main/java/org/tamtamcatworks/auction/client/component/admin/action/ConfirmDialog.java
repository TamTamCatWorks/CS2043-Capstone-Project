package org.tamtamcatworks.auction.client.component.admin.action;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class ConfirmDialog {

    private ConfirmDialog() {}

    public static boolean show(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        return alert.showAndWait()

                .filter(ButtonType.OK::equals)

                .isPresent();
    }
}