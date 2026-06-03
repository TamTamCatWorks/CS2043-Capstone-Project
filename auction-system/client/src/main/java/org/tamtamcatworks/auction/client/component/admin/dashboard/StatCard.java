package org.tamtamcatworks.auction.client.component.admin.dashboard;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatCard extends VBox {

    private final Label titleLabel =
            new Label();

    private final Label valueLabel =
            new Label();

    public StatCard(
            String title,
            String value
    ) {

        setSpacing(10);

        setPadding(
                new Insets(20)
        );

        getStyleClass().addAll(

                "admin-card",
                "stat-card"
        );

        titleLabel.setText(title);

        titleLabel.getStyleClass().add(
                "stat-card-title"
        );

        valueLabel.setText(value);

        valueLabel.getStyleClass().add(
                "stat-card-value"
        );

        getChildren().addAll(

                titleLabel,
                valueLabel
        );
    }

    public void setValue(
            String value
    ) {

        valueLabel.setText(value);
    }
}