package org.tamtamcatworks.auction.client.controller.shell;

import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.tamtamcatworks.auction.client.AppContext;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.NavigationState;
import org.tamtamcatworks.auction.client.SearchParams;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.client.ViewLoader;
import org.tamtamcatworks.auction.client.controller.auction.SearchResultsController;
import org.tamtamcatworks.auction.shared.response.UserResponse;

/**
 * Shell controller for the dashboard layout.
 *
 * <h3>Single search entry point</h3>
 * <p>The global header search ({@code headerSearchField} + {@code headerCategoryFilter})
 * is the <em>only</em> place the user types a query.  Child views
 * ({@code auctions-list.fxml}, {@code search-results.fxml}) do not own their
 * own search fields.
 *
 * <h3>Navigation to search results</h3>
 * <ul>
 *   <li>If search-results is not the active view, {@link #navigateToSearchResults(SearchParams)}
 *       loads it into {@code mainContentArea} via {@link ViewLoader}, retrieves
 *       the controller, and calls {@link SearchResultsController#initWithParams}.</li>
 *   <li>If search-results is already active, it calls {@code initWithParams} on the
 *       cached controller — no view reload, results update in-place.</li>
 * </ul>
 *
 * <h3>Back-sync from child controllers</h3>
 * <p>When the user changes filters <em>inside</em> search-results, the child calls
 * {@link #syncHeaderSearch(String, String)} to push the new state back into the
 * header controls so they stay consistent.
 */
public class LayoutController {

  private static final PseudoClass HEADER_SEARCH_ACTIVE =
      PseudoClass.getPseudoClass("header-search-active");

  // ── FXML fields ──────────────────────────────────────────────────────────
  @FXML private MenuButton         userMenuButton;
  @FXML private MenuItem           userNameItem;
  @FXML private MenuItem           userEmailItem;
  @FXML private MenuItem           adminPanelItem;
  @FXML private MenuItem           notificationsHeaderItem;
  @FXML private SeparatorMenuItem  notificationsSeparatorItem;
  @FXML private HBox               headerSearchShell;
  @FXML private TextField          headerSearchField;
  @FXML private ComboBox<String>   headerCategoryFilter;
  @FXML private Button             headerCreateAuctionButton;
  @FXML private StackPane          mainContentArea;

  // ── Internal state ───────────────────────────────────────────────────────
  private NotificationMenuManager  notificationMenuManager;

  /** Cached reference to the SearchResultsController when it is the active view. */
  private SearchResultsController  activeSearchController;

  /** 350 ms debounce for live-typing in the header search field. */
  private final PauseTransition    searchDebounce =
      new PauseTransition(Duration.millis(350));

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    // Register ourselves in AppContext so child controllers can reach us.
    AppContext.setLayoutController(this);

    UserResponse user = SessionManager.getCurrentUser();
    updateUserMenu(user);
    SessionManager.currentUserProperty().addListener(
        (obs, old, updated) -> updateUserMenu(updated));

    notificationMenuManager = new NotificationMenuManager(
        userMenuButton, notificationsHeaderItem, notificationsSeparatorItem);

    // ── Category filter ──
    if (headerCategoryFilter != null) {
      headerCategoryFilter.getItems().setAll(
          "All categories", "Art", "Electronics", "Vehicle", "Other");
      headerCategoryFilter.setValue("All categories");
      headerCategoryFilter.showingProperty().addListener(
          (obs, was, is) -> updateHeaderSearchActive());
      headerCategoryFilter.setOnMousePressed(e -> {
        if (!headerCategoryFilter.isShowing()) {
          headerCategoryFilter.show();
          e.consume();
        }
      });
      // Changing category fires search immediately
      headerCategoryFilter.setOnAction(e -> handleHeaderSearchCommitted());
    }

    // ── Search field ──
    if (headerSearchField != null) {
      headerSearchField.setOnAction(e -> handleHeaderSearchCommitted());
      headerSearchField.textProperty().addListener((obs, old, nv) -> {
        updateHeaderSearchActive();
        searchDebounce.playFromStart();
      });
      headerSearchField.focusedProperty().addListener(
          (obs, was, is) -> updateHeaderSearchActive());
    }

    searchDebounce.setOnFinished(e -> handleHeaderSearchCommitted());

    if (headerCreateAuctionButton != null) {
      headerCreateAuctionButton.setOnAction(e -> handleHeaderCreateAuction());
    }

    updateHeaderSearchActive();
    notificationMenuManager.start();
  }

  // ── Public API for child controllers ─────────────────────────────────────

  /**
   * Navigate to search-results, or update results in-place if already active,
   * using the given {@link SearchParams}.
   *
   * <p>This is the single navigation method used by every "See all →" link
   * and pill button in child views.
   */
  public void navigateToSearchResults(SearchParams params) {
    // Sync the header controls to reflect the params being applied
    syncHeaderSearch(params.query(), params.category());

    if (activeSearchController != null && isSearchResultsActive()) {
      // Already showing search results — update in-place
      activeSearchController.initWithParams(params);
    } else {
      // Load search-results.fxml, capture its controller, then initialise
      activeSearchController =
          ViewLoader.into(mainContentArea)
                    .loadWithController("/fxml/search-results.fxml");
      activeSearchController.initWithParams(params);
    }
  }

  /**
   * Back-sync called by {@link SearchResultsController} whenever the user
   * changes a filter inside the results page.  Keeps the header controls
   * consistent with what is currently being searched.
   *
   * @param query    current search text (may be empty)
   * @param category current category label (e.g. "Art", "All categories")
   */
  public void syncHeaderSearch(String query, String category) {
    if (headerSearchField != null) {
      // Temporarily disconnect the debounce to avoid a feedback loop
      searchDebounce.stop();
      headerSearchField.setText(query == null ? "" : query);
    }
    if (headerCategoryFilter != null) {
      String cat = (category == null || category.isBlank()) ? "All categories" : category;
      // Only update if the value actually changed to avoid spurious onAction events
      if (!cat.equals(headerCategoryFilter.getValue())) {
        headerCategoryFilter.setOnAction(null);          // suppress re-trigger
        headerCategoryFilter.setValue(cat);
        headerCategoryFilter.setOnAction(e -> handleHeaderSearchCommitted());
      }
    }
  }

  /** Called when the active view changes away from search-results. */
  public void clearActiveSearchController() {
    activeSearchController = null;
  }

  // ── Navigation handlers ───────────────────────────────────────────────────

  @FXML
  private void handleNavDashboard() {
    activeSearchController = null;
    Navigation.navigateTo("/fxml/dashboard.fxml");
  }

  @FXML
  private void handleHeaderBrandClicked() {
    activeSearchController = null;
    Navigation.navigateTo("/fxml/auctions-list.fxml");
  }

  @FXML
  private void handleHeaderCreateAuction() {
    Navigation.navigateTo("/fxml/create-auction.fxml");
  }

  @FXML
  void handleHeaderSearchCommitted() {
    if (headerSearchField == null) return;

    String query    = headerSearchField.getText() != null
                      ? headerSearchField.getText().trim() : "";
    String category = headerCategoryFilter != null
                      ? headerCategoryFilter.getValue() : "All categories";

    NavigationState.addRecentSearch(query.isEmpty() ? category : query);

    SearchParams params = SearchParams.forQueryAndCategory(query, category);
    navigateToSearchResults(params);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  /** @return true if the current child in {@code mainContentArea} is the search-results view. */
  private boolean isSearchResultsActive() {
    return mainContentArea != null
        && !mainContentArea.getChildren().isEmpty()
        && activeSearchController != null;
  }

  private void updateHeaderSearchActive() {
    if (headerSearchShell == null) return;
    boolean focused = headerSearchField != null && headerSearchField.isFocused();
    boolean showing = headerCategoryFilter != null && headerCategoryFilter.isShowing();
    headerSearchShell.pseudoClassStateChanged(HEADER_SEARCH_ACTIVE, focused || showing);
  }

  private void updateUserMenu(UserResponse user) {
    boolean isAdmin = user != null && user.isAdmin();
    if (user == null) {
      userMenuButton.setText("");
      userMenuButton.setAccessibleText("");
      userNameItem.setText("");
      userEmailItem.setText("");
      if (adminPanelItem != null) {
        adminPanelItem.setVisible(false);
        adminPanelItem.setDisable(true);
      }
      return;
    }
    userMenuButton.setText("");
    userMenuButton.setAccessibleText(user.fullName());
    userNameItem.setText(user.fullName());
    userEmailItem.setText(user.email());
    if (adminPanelItem != null) {
      adminPanelItem.setVisible(isAdmin);
      adminPanelItem.setDisable(!isAdmin);
    }
  }

  @FXML
  private void handleLogout() {
    notificationMenuManager.stop();
    activeSearchController = null;
    SessionManager.logout();
    Navigation.navigateTo("/fxml/login.fxml");
  }

  @FXML
  private void handleUserMenuOpened() {
    notificationMenuManager.handleMenuOpened();
  }

  @FXML
  private void handleOpenNotificationsFromMenu() {
    NavigationState.setDashboardViewPath("/fxml/dashboard/notifications.fxml");
    Navigation.navigateTo("/fxml/dashboard.fxml");
  }

  @FXML
  private void handleOpenAdminPanel() {
    NavigationState.setDashboardViewPath(
        "/fxml/admin/dashboard/admin-dashboard.fxml"
    );

    Navigation.navigateTo("/fxml/dashboard.fxml");
  }
}
