package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.tamtamcatworks.auction.client.ViewLoader;

/**
 * Controller for the home sub-panel within the dashboard.
 *
 * <p>Hosts two tabs (Auctions / Bids) that swap a nested content area. Uses {@link TabChipBar} for
 * selection state and {@link ViewLoader} for sub-view injection — no copy-pasted loadView() or
 * applySelected() methods.
 */
public class HomeViewController {

  @FXML private StackPane nestedContentArea;
  @FXML private Button homeAuctionsButton;
  @FXML private Button homeBidsButton;

  private TabChipBar tabs;

  @FXML
  public void initialize() {
    tabs = new TabChipBar("profile-tab-chip-selected", homeAuctionsButton, homeBidsButton);
    tabs.select(homeAuctionsButton);
    ViewLoader.into(nestedContentArea).load("/fxml/dashboard/auctions.fxml");
  }

  @FXML
  private void handleShowAuctions() {
    tabs.select(homeAuctionsButton);
    ViewLoader.into(nestedContentArea).load("/fxml/dashboard/auctions.fxml");
  }

  @FXML
  private void handleShowBids() {
    tabs.select(homeBidsButton);
    ViewLoader.into(nestedContentArea).load("/fxml/dashboard/bids.fxml");
  }
}
