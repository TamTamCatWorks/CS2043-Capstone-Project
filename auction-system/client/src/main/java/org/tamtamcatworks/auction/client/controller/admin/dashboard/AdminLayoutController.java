package org.tamtamcatworks.auction.client.controller.admin.dashboard;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.tamtamcatworks.auction.client.ViewLoader;
import org.tamtamcatworks.auction.client.controller.BaseController;

public class AdminLayoutController extends BaseController {

  @FXML private VBox sidebar;

  @FXML private StackPane contentArea;

  @FXML
  public void initialize() {

    loadDashboard();
  }

  private void loadDashboard() {

    ViewLoader.into(contentArea).load("/fxml/admin/dashboard/admin-dashboard.fxml");
  }
}
