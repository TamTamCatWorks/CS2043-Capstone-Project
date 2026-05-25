package org.tamtamcatworks.auction.client.controller.shell;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.SeparatorMenuItem;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class LayoutController {

    private static final PseudoClass HEADER_SEARCH_ACTIVE = PseudoClass.getPseudoClass("header-search-active");

    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem userNameItem;
    @FXML private MenuItem userEmailItem;
    @FXML private MenuItem notificationsHeaderItem;
    @FXML private SeparatorMenuItem notificationsSeparatorItem;
    @FXML private HBox headerSearchShell;
    @FXML private TextField headerSearchField;
    @FXML private ComboBox<String> headerCategoryFilter;
    @FXML private Button headerCreateAuctionButton;

    private NotificationMenuManager notificationMenuManager;

    @FXML
    public void initialize() {
        UserResponse user = SessionManager.getCurrentUser();
        if (user != null) {
            userMenuButton.setText("");
            userMenuButton.setAccessibleText(user.fullName());
            userNameItem.setText(user.fullName());
            userEmailItem.setText(user.email());
        }

        notificationMenuManager = new NotificationMenuManager(
            userMenuButton, notificationsHeaderItem, notificationsSeparatorItem);

        if (headerCategoryFilter != null) {
            headerCategoryFilter.getItems().setAll("All categories", "Art", "Electronics", "Vehicle", "Other");
            headerCategoryFilter.setValue("All categories");
            headerCategoryFilter.showingProperty().addListener((obs, wasShowing, isShowing) -> updateHeaderSearchActive());
            headerCategoryFilter.setOnMousePressed(e -> {
                if (!headerCategoryFilter.isShowing()) {
                    headerCategoryFilter.show();
                    e.consume();
                }
            });
        }

        if (headerCreateAuctionButton != null) {
            headerCreateAuctionButton.setOnAction(e -> handleHeaderCreateAuction());
        }

        if (headerSearchField != null) {
            headerSearchField.setOnAction(e -> handleHeaderSearchCommitted());
            headerSearchField.textProperty().addListener((obs, oldValue, newValue) -> updateHeaderSearchActive());
            headerSearchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateHeaderSearchActive());
        }

        updateHeaderSearchActive();

        // Populate the combined account menu and start polling notifications.
        notificationMenuManager.start();
    }

    // ── Navigation Handlers ──────────────────────────────────────────────────

    @FXML
    private void handleNavDashboard() {
        Navigation.navigateTo("/fxml/dashboard.fxml");
    }

    @FXML
    private void handleHeaderBrandClicked() {
        Navigation.navigateTo("/fxml/auctions-list.fxml");
    }

    @FXML
    private void handleHeaderCreateAuction() {
        Navigation.navigateTo("/fxml/create-auction.fxml");
    }

    @FXML
    private void handleHeaderSearchCommitted() {
        if (headerSearchField == null) {
            return;
        }

        String query = headerSearchField.getText() != null ? headerSearchField.getText().trim() : "";
        String category = headerCategoryFilter != null ? headerCategoryFilter.getValue() : "All categories";
        SessionManager.addRecentSearch(query.isEmpty() ? category : query);
        SessionManager.setPendingSearch(query, category);
        Navigation.navigateTo("/fxml/search-results.fxml");
    }

    private void updateHeaderSearchActive() {
        if (headerSearchShell == null) {
            return;
        }

        boolean focused = headerSearchField != null && headerSearchField.isFocused();
        boolean showing = headerCategoryFilter != null && headerCategoryFilter.isShowing();

        boolean active = focused || showing;
        headerSearchShell.pseudoClassStateChanged(HEADER_SEARCH_ACTIVE, active);
    }

    @FXML
    private void handleLogout() {
        notificationMenuManager.stop();
        SessionManager.logout();
        Navigation.navigateTo("/fxml/login.fxml");
    }

    @FXML
    private void handleUserMenuOpened() {
        notificationMenuManager.handleMenuOpened();
    }

    @FXML
    private void handleOpenNotificationsFromMenu() {
        // Request dashboard to open the Notifications view and navigate there
        SessionManager.setDashboardViewPath("/fxml/dashboard/notifications.fxml");
        Navigation.navigateTo("/fxml/dashboard.fxml");
    }
}
