package org.tamtamcatworks.auction.client.component.admin.feedback;

import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

public class LoadingOverlay
        extends StackPane {

    public LoadingOverlay() {

        setStyle(

                "-fx-background-color: rgba(0,0,0,0.25);"
        );

        ProgressIndicator indicator =
                new ProgressIndicator();

        setAlignment(Pos.CENTER);

        getChildren().add(indicator);

        setVisible(false);

        setManaged(false);
    }

    public void show() {

        setVisible(true);

        setManaged(true);
    }

    public void hide() {

        setVisible(false);

        setManaged(false);
    }
}