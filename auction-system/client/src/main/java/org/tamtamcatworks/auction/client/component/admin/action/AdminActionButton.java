package org.tamtamcatworks.auction.client.component.admin.action;

import javafx.scene.control.Button;

public class AdminActionButton
        extends Button {

    public AdminActionButton(
            String text
    ) {

        super(text);

        getStyleClass().addAll(

            "primary-button"
        );
    }
}