package org.tamtamcatworks.auction.client.component.admin.table;

import javafx.scene.control.TableView;

public class AdminTableView<T> extends TableView<T> {

  public AdminTableView() {

    getStyleClass().add("admin-table");

    setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    setPlaceholder(new javafx.scene.control.Label("No data available"));
  }
}
