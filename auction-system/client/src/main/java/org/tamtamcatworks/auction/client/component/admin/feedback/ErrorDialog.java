package org.tamtamcatworks.auction.client.component.admin.feedback;

import javafx.scene.control.Alert;

public final class ErrorDialog {

    private ErrorDialog() {}

    public static void show(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle("Error");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}