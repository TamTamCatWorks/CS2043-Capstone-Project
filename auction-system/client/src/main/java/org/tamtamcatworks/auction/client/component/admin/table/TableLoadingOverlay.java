package org.tamtamcatworks.auction.client.component.admin.table;

import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

public class TableLoadingOverlay
        extends StackPane {

    public TableLoadingOverlay() {

        setAlignment(Pos.CENTER);

        getChildren().add(
                new ProgressIndicator()
        );

        setVisible(false);
    }
}