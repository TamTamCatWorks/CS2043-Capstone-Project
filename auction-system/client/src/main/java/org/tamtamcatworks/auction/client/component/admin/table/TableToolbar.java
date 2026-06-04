package org.tamtamcatworks.auction.client.component.admin.table;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TableToolbar extends HBox {

  private final TextField searchField = new TextField();

  private final Button refreshButton = new Button("Refresh");

  public TableToolbar() {

    setSpacing(10);

    setAlignment(Pos.CENTER_LEFT);

    getStyleClass().add("table-toolbar");

    searchField.setPromptText("Search...");

    getChildren().addAll(searchField, refreshButton);
  }

  public TextField getSearchField() {

    return searchField;
  }

  public Button getRefreshButton() {

    return refreshButton;
  }
}
