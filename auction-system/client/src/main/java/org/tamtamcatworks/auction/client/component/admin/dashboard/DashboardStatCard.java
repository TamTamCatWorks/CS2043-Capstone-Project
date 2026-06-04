package org.tamtamcatworks.auction.client.component.admin.dashboard;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardStatCard extends VBox {

  private final Label valueLabel = new Label();

  public DashboardStatCard(String title, String value) {

    Label titleLabel = new Label(title);

    valueLabel.setText(value);

    titleLabel.getStyleClass().add("dashboard-card-title");

    valueLabel.getStyleClass().add("dashboard-card-value");

    setSpacing(10);

    setAlignment(Pos.CENTER_LEFT);

    getStyleClass().add("dashboard-stat-card");

    getChildren().addAll(titleLabel, valueLabel);
  }

  public void setValue(String value) {

    valueLabel.setText(value);
  }
}
