package org.tamtamcatworks.auction.client.controller.shell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class HomeViewController {
    @FXML private StackPane nestedContentArea;
    @FXML private Button homeAuctionsButton;
    @FXML private Button homeBidsButton;

    @FXML
    public void initialize() {
        setSelectedTab(homeAuctionsButton);
        loadNestedView("/fxml/dashboard/auctions.fxml");
    }

    @FXML
    private void handleShowAuctions() {
        setSelectedTab(homeAuctionsButton);
        loadNestedView("/fxml/dashboard/auctions.fxml");
    }

    @FXML
    private void handleShowBids() {
        setSelectedTab(homeBidsButton);
        loadNestedView("/fxml/dashboard/bids.fxml");
    }

    private void setSelectedTab(Button selectedButton) {
        applySelected(homeAuctionsButton, homeAuctionsButton == selectedButton);
        applySelected(homeBidsButton, homeBidsButton == selectedButton);
    }

    private void applySelected(Button button, boolean selected) {
        if (button == null) {
            return;
        }
        button.getStyleClass().remove("profile-tab-chip-selected");
        if (selected) {
            button.getStyleClass().add("profile-tab-chip-selected");
        }
    }

    private void loadNestedView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            nestedContentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
