package org.tamtamcatworks.auction.client.controller.auction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.tamtamcatworks.auction.client.AppContext;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SearchParams;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.PageResponse;

/**
 * Controller for {@code search-results.fxml}.
 *
 * <h3>Architecture</h3>
 * <p>This controller owns <em>no search field</em>.  The global header search
 * in {@code LayoutController} is the single entry point.  This controller:
 * <ul>
 *   <li>Receives its initial parameters via
 *       {@link #initWithParams(SearchParams)} called by {@code LayoutController}
 *       after each navigation or live-type debounce.</li>
 *   <li>Owns the in-page refinements: status filter, category filter, sort
 *       combo, grid/list toggle, and pagination.</li>
 *   <li>Calls back to {@code LayoutController.syncHeaderSearch()} whenever an
 *       in-page filter change alters the effective query or category, keeping
 *       the header in sync.</li>
 * </ul>
 */
@Route(fxml = "/fxml/search-results.fxml", layout = Route.DASHBOARD_LAYOUT)
public class SearchResultsController extends BaseController {

  // ── FXML fields ───────────────────────────────────────────────────────────

  // Filter / sort bar
  @FXML private ComboBox<String> statusFilter;
  @FXML private ComboBox<String> categoryFilter;
  @FXML private ComboBox<String> sortCombo;
  @FXML private HBox             activeFilterChips;
  @FXML private Hyperlink        clearAllFilters;
  @FXML private ToggleButton     gridViewToggle;
  @FXML private ToggleButton     listViewToggle;
  @FXML private Label            subtitleLabel;
  @FXML private Label            resultCountLabel;

  // States
  @FXML private StackPane        loadingPane;
  @FXML private StackPane        emptyPane;
  @FXML private Label            errorLabel;
  @FXML private Label            emptyTitleLabel;
  @FXML private Label            emptySubLabel;
  @FXML private Button           clearFiltersButton;

  // Results
  @FXML private StackPane        resultsArea;
  @FXML private FlowPane         auctionsContainer;
  @FXML private VBox             auctionsListContainer;

  // Pagination
  @FXML private HBox             pageNumbersContainer;
  @FXML private Button           firstPageButton;
  @FXML private Button           prevPageButton;
  @FXML private Button           nextPageButton;
  @FXML private Button           lastPageButton;
  @FXML private ComboBox<String> pageSizeCombo;

  // ── Internal state ────────────────────────────────────────────────────────

  /** The free-text query currently being searched (owned by LayoutController's field). */
  private String currentQuery = "";

  private List<AuctionResponse> allResults     = new ArrayList<>();
  private List<AuctionResponse> sortedResults  = new ArrayList<>();
  private int  currentPage   = 0;
  private int  pageSize      = 20;
  private int  totalPages    = 1;
  private long totalElements = 0;
  private boolean gridMode   = true;

  /** Flag: suppress back-sync calls while initWithParams() is populating combos. */
  private boolean suppressSync = false;

  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    // Status filter
    statusFilter.getItems().addAll("ALL", "ACTIVE", "PENDING", "CLOSED");
    statusFilter.setValue("ALL");
    statusFilter.setOnAction(e -> {
      if (!suppressSync) {
        currentPage = 0;
        pushFilterSyncToHeader();
        executeSearch();
      }
    });

    // Category filter
    categoryFilter.getItems().addAll("All categories", "Art", "Electronics", "Vehicle", "Other");
    categoryFilter.setValue("All categories");
    categoryFilter.setOnAction(e -> {
      if (!suppressSync) {
        currentPage = 0;
        pushFilterSyncToHeader();
        executeSearch();
      }
    });

    // Sort combo
    sortCombo.getItems().addAll(
        "Newest First", "Oldest First", "Ending Soon",
        "Most Bids", "Price: Low to High", "Price: High to Low");
    sortCombo.setValue("Newest First");
    sortCombo.setOnAction(e -> { if (!suppressSync) { currentPage = 0; applySortAndRender(); } });

    // Page size
    pageSizeCombo.getItems().addAll("10", "20", "50");
    pageSizeCombo.setValue("20");
    pageSizeCombo.setOnAction(e -> {
      try { pageSize = Integer.parseInt(pageSizeCombo.getValue()); }
      catch (Exception ex) { pageSize = 20; }
      currentPage = 0;
      applySortAndRender();
    });

    // Pagination nav buttons
    prevPageButton.setOnAction(e ->  { if (currentPage > 0) { currentPage--; applySortAndRender(); } });
    nextPageButton.setOnAction(e ->  { if (currentPage + 1 < totalPages) { currentPage++; applySortAndRender(); } });
    firstPageButton.setOnAction(e -> { currentPage = 0; applySortAndRender(); });
    lastPageButton.setOnAction(e ->  { currentPage = Math.max(0, totalPages - 1); applySortAndRender(); });

    // Clear-all link + empty-pane button
    clearAllFilters.setOnAction(e -> clearAllFiltersAction());
    clearFiltersButton.setOnAction(e -> clearAllFiltersAction());

    // View toggle
    ToggleGroup viewGroup = new ToggleGroup();
    gridViewToggle.setToggleGroup(viewGroup);
    listViewToggle.setToggleGroup(viewGroup);
    gridViewToggle.setSelected(true);
    gridViewToggle.setOnAction(e -> switchView(true));
    listViewToggle.setOnAction(e -> switchView(false));
  }

  // ── Public entry-point ────────────────────────────────────────────────────

  /**
   * Called by {@link org.tamtamcatworks.auction.client.controller.shell.LayoutController}
   * whenever a new search is triggered (from the header, a shelf "See all →" link,
   * or a live-type debounce while this view is already active).
   *
   * <p>Populates filter combos from {@code params} without triggering recursive
   * back-syncs, then calls {@link #executeSearch()}.
   */
  public void initWithParams(SearchParams params) {
    suppressSync = true;
    try {
      currentQuery = params.query() != null ? params.query() : "";
      currentPage  = params.page();

      // Sync status filter
      String status = params.status() != null ? params.status() : "ALL";
      if (!statusFilter.getItems().contains(status)) status = "ALL";
      statusFilter.setValue(status);

      // Sync category filter
      String cat = params.category() != null ? params.category() : "All categories";
      if (!categoryFilter.getItems().contains(cat)) cat = "All categories";
      categoryFilter.setValue(cat);

      // Sync sort combo
      String sort = params.sortOrder() != null ? params.sortOrder() : "Newest First";
      if (!sortCombo.getItems().contains(sort)) sort = "Newest First";
      sortCombo.setValue(sort);

    } finally {
      suppressSync = false;
    }

    updateSubtitle(currentQuery, categoryFilter.getValue(), statusFilter.getValue());
    executeSearch();
  }

  // ── Search / render pipeline ──────────────────────────────────────────────

  /**
   * Fetch from the backend using the current combo values and {@code currentQuery},
   * then pipe results through {@link #applySortAndRender()}.
   */
  private void executeSearch() {
    setLoading(true);
    hideError();
    emptyPane.setVisible(false);
    emptyPane.setManaged(false);

    String q      = currentQuery;
    String status = statusFilter.getValue();
    String cat    = categoryFilter.getValue();

    updateSubtitle(q, cat, status);

    // Fetch a large page; all paging/sorting is done client-side for responsiveness.
    final int fetchSize = 200;

    AsyncTask.<PageResponse<AuctionResponse>>run(() ->
            api.searchAuctionsPaged(q, status, cat, 0, fetchSize))
        .onSuccess(page -> {
          setLoading(false);
          allResults.clear();
          if (page != null && page.content() != null) allResults.addAll(page.content());
          applySortAndRender();
        })
        .onFailure(ex -> {
          setLoading(false);
          errorLabel.setText("Search failed: " + (ex != null ? ex.getMessage() : "Unknown"));
          errorLabel.setVisible(true);
          errorLabel.setManaged(true);
        })
        .start();
  }

  /**
   * Sort {@code allResults}, slice into the current page, render, rebuild pagination.
   */
  private void applySortAndRender() {
    sortedResults  = applySortOrder(new ArrayList<>(allResults));
    totalElements  = sortedResults.size();
    totalPages     = (int) Math.max(1, Math.ceil((double) totalElements / pageSize));
    currentPage    = Math.min(currentPage, Math.max(0, totalPages - 1));

    int from = currentPage * pageSize;
    int to   = (int) Math.min(from + pageSize, totalElements);
    List<AuctionResponse> pageItems = from < sortedResults.size()
        ? sortedResults.subList(from, to) : List.of();

    resultCountLabel.setText(totalElements + " result" + (totalElements == 1 ? "" : "s"));
    rebuildFilterChips();
    renderResults(pageItems);
    buildPageNumbers(currentPage, totalPages);
    updateNavButtons();
  }

  // ── Sort ──────────────────────────────────────────────────────────────────

  private List<AuctionResponse> applySortOrder(List<AuctionResponse> source) {
    String sort = sortCombo.getValue();
    if (sort == null) sort = "Newest First";
    Comparator<AuctionResponse> cmp = switch (sort) {
      case "Oldest First"       -> Comparator.comparing(
                                       a -> a.startTime() != null ? a.startTime() : LocalDateTime.MIN);
      case "Ending Soon"        -> Comparator.comparing(
                                       a -> a.endTime() != null ? a.endTime() : LocalDateTime.MAX);
      case "Most Bids"          -> Comparator.comparingDouble(
                                       (AuctionResponse a) -> a.currentPrice()).reversed();
      case "Price: Low to High" -> Comparator.comparingDouble(AuctionResponse::currentPrice);
      case "Price: High to Low" -> Comparator.comparingDouble(
                                       (AuctionResponse a) -> a.currentPrice()).reversed();
      default                   -> Comparator.comparing(
                                       (AuctionResponse a) ->
                                           a.startTime() != null ? a.startTime() : LocalDateTime.MIN,
                                       Comparator.reverseOrder());
    };
    source.sort(cmp);
    return source;
  }

  // ── Render ────────────────────────────────────────────────────────────────

  private void renderResults(List<AuctionResponse> items) {
    auctionsContainer.getChildren().clear();
    auctionsListContainer.getChildren().clear();

    if (items == null || items.isEmpty()) {
      emptyPane.setVisible(true);
      emptyPane.setManaged(true);
      return;
    }
    emptyPane.setVisible(false);
    emptyPane.setManaged(false);

    if (gridMode) {
      for (AuctionResponse a : items)
        auctionsContainer.getChildren().add(AuctionCardFactory.createAuctionCard(a));
    } else {
      for (AuctionResponse a : items)
        auctionsListContainer.getChildren().add(createListCard(a));
    }
  }

  // ── List-view card ────────────────────────────────────────────────────────

  private HBox createListCard(AuctionResponse a) {
    HBox card = new HBox(16);
    card.getStyleClass().add("list-card");
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPrefHeight(90);
    card.setMinHeight(90);
    card.setMaxHeight(90);
    card.setOnMouseClicked(e -> {
      Navigation.setContextData(a.id());
      Navigation.navigateTo("/fxml/auction-detail.fxml");
    });

    // Thumbnail
    StackPane imgWrap = new StackPane();
    imgWrap.getStyleClass().add("list-card-thumb");
    imgWrap.setPrefWidth(120);
    imgWrap.setMinWidth(120);
    imgWrap.setMaxWidth(120);
    imgWrap.setPrefHeight(90);

    ImageView imgView = new ImageView();
    imgView.setFitWidth(120);
    imgView.setFitHeight(90);
    imgView.setPreserveRatio(true);

    Label noImg = new Label("No Image");
    noImg.getStyleClass().add("text-muted");
    noImg.setStyle("-fx-font-size: 8pt;");

    if (a.imageUrl() != null && !a.imageUrl().isEmpty()) {
      try { imgView.setImage(new Image(a.imageUrl(), true)); noImg.setVisible(false); }
      catch (Exception ex) { imgView.setVisible(false); }
    } else {
      imgView.setVisible(false);
    }
    imgWrap.getChildren().addAll(imgView, noImg);

    // Centre details column
    VBox details = new VBox(4);
    HBox.setHgrow(details, Priority.ALWAYS);
    details.setAlignment(Pos.CENTER_LEFT);

    String itemType = a.itemType() != null ? a.itemType() : "OTHER";
    Label badge = new Label(itemType.toUpperCase());
    badge.getStyleClass().addAll("category-badge", "category-" + itemType.toLowerCase());

    Label title = new Label(a.title());
    title.getStyleClass().add("asset-card-title");

    Label seller = new Label("by " + (a.sellerName() != null ? a.sellerName() : "Unknown"));
    seller.getStyleClass().add("asset-card-seller");
    details.getChildren().addAll(badge, title, seller);

    // Right column: price + status + time
    VBox priceTime = new VBox(4);
    priceTime.setAlignment(Pos.CENTER_RIGHT);
    priceTime.setMinWidth(110);

    Label price = new Label(String.format("$%.2f", a.currentPrice()));
    price.getStyleClass().add("asset-card-price");

    Label statusBadge = new Label(a.status() != null ? a.status() : "");
    statusBadge.getStyleClass().addAll("status-badge", getStatusClass(a.status()));

    Label timeLabel = new Label(formatTimeInfo(a));
    timeLabel.getStyleClass().add("auction-time");

    priceTime.getChildren().addAll(price, statusBadge, timeLabel);

    card.getChildren().addAll(imgWrap, details, priceTime);
    return card;
  }

  // ── Active filter chips ───────────────────────────────────────────────────

  private void rebuildFilterChips() {
    activeFilterChips.getChildren().clear();
    boolean any = false;

    String q   = currentQuery;
    String cat = categoryFilter.getValue();
    String st  = statusFilter.getValue();

    if (q != null && !q.isBlank()) {
      activeFilterChips.getChildren().add(buildChip("\"" + q + "\"", () -> {
        currentQuery = "";
        // Tell the layout controller to clear the header search field
        safeSync("", cat);
        currentPage = 0;
        executeSearch();
      }));
      any = true;
    }
    if (cat != null && !cat.isBlank() && !"All categories".equalsIgnoreCase(cat)) {
      activeFilterChips.getChildren().add(buildChip(cat, () -> {
        suppressSync = true;
        categoryFilter.setValue("All categories");
        suppressSync = false;
        safeSync(currentQuery, "All categories");
        currentPage = 0;
        executeSearch();
      }));
      any = true;
    }
    if (st != null && !"ALL".equalsIgnoreCase(st)) {
      activeFilterChips.getChildren().add(buildChip(st, () -> {
        suppressSync = true;
        statusFilter.setValue("ALL");
        suppressSync = false;
        currentPage = 0;
        executeSearch();
      }));
      any = true;
    }

    clearAllFilters.setVisible(any);
    clearAllFilters.setManaged(any);
  }

  private HBox buildChip(String label, Runnable onRemove) {
    HBox chip = new HBox(5);
    chip.getStyleClass().add("filter-chip");
    chip.setAlignment(Pos.CENTER_LEFT);

    Label lbl = new Label(label);
    lbl.getStyleClass().add("filter-chip-label");

    Button x = new Button("×");
    x.getStyleClass().add("filter-chip-remove");
    x.setOnAction(e -> onRemove.run());

    chip.getChildren().addAll(lbl, x);
    return chip;
  }

  private void clearAllFiltersAction() {
    currentQuery = "";
    suppressSync = true;
    statusFilter.setValue("ALL");
    categoryFilter.setValue("All categories");
    suppressSync = false;
    safeSync("", "All categories");
    currentPage = 0;
    executeSearch();
  }

  // ── View toggle ───────────────────────────────────────────────────────────

  private void switchView(boolean toGrid) {
    gridMode = toGrid;
    auctionsContainer.setVisible(toGrid);
    auctionsContainer.setManaged(toGrid);
    auctionsListContainer.setVisible(!toGrid);
    auctionsListContainer.setManaged(!toGrid);

    // Re-render current page without re-fetching
    int from = currentPage * pageSize;
    int to   = (int) Math.min(from + pageSize, sortedResults.size());
    List<AuctionResponse> pageItems = from < sortedResults.size()
        ? sortedResults.subList(from, to) : List.of();
    renderResults(pageItems);
  }

  // ── Pagination ────────────────────────────────────────────────────────────

  private void buildPageNumbers(int current, int total) {
    pageNumbersContainer.getChildren().clear();
    if (total <= 1) return;

    List<Integer> slots = new ArrayList<>();
    slots.add(0);
    int lo = Math.max(1, current - 2);
    int hi = Math.min(total - 2, current + 2);
    if (lo > 1)         slots.add(-1);   // left ellipsis
    for (int i = lo; i <= hi; i++) slots.add(i);
    if (hi < total - 2) slots.add(-1);   // right ellipsis
    if (total > 1)      slots.add(total - 1);

    for (int p : slots) {
      if (p == -1) {
        Label ellipsis = new Label("…");
        ellipsis.getStyleClass().add("text-muted");
        ellipsis.setMinWidth(28);
        ellipsis.setAlignment(Pos.CENTER);
        pageNumbersContainer.getChildren().add(ellipsis);
      } else {
        Button btn = new Button(String.valueOf(p + 1));
        btn.setMinWidth(34);
        btn.getStyleClass().add(p == current ? "page-btn-active" : "btn-ghost");
        final int page = p;
        btn.setOnAction(e -> { currentPage = page; applySortAndRender(); });
        pageNumbersContainer.getChildren().add(btn);
      }
    }
  }

  private void updateNavButtons() {
    prevPageButton.setDisable(currentPage <= 0);
    nextPageButton.setDisable(currentPage + 1 >= totalPages);
    firstPageButton.setDisable(currentPage <= 0);
    lastPageButton.setDisable(currentPage + 1 >= totalPages);
  }

  // ── Subtitle animation ────────────────────────────────────────────────────

  private void updateSubtitle(String q, String cat, String status) {
    boolean hasQ   = q != null && !q.isBlank();
    boolean hasCat = cat != null && !cat.isBlank() && !"All categories".equalsIgnoreCase(cat);
    boolean hasSt  = status != null && !"ALL".equalsIgnoreCase(status);

    String text;
    if (hasQ && hasCat) text = "Results for \"" + q + "\" in " + cat;
    else if (hasQ)      text = "Showing results for \"" + q + "\"";
    else if (hasCat)    text = "Browsing " + cat;
    else if (hasSt)     text = "Status: " + status;
    else                text = "All auctions";

    FadeTransition out = new FadeTransition(Duration.millis(150), subtitleLabel);
    out.setFromValue(1.0);
    out.setToValue(0.0);
    out.setOnFinished(e -> {
      subtitleLabel.setText(text);
      FadeTransition in = new FadeTransition(Duration.millis(150), subtitleLabel);
      in.setFromValue(0.0);
      in.setToValue(1.0);
      in.play();
    });
    out.play();
  }

  // ── Back-sync to LayoutController ─────────────────────────────────────────

  /**
   * Called whenever the user changes a filter inside this page.
   * Pushes the effective query + category back up to the header field.
   */
  private void pushFilterSyncToHeader() {
    safeSync(currentQuery, categoryFilter.getValue());
    updateSubtitle(currentQuery, categoryFilter.getValue(), statusFilter.getValue());
  }

  private void safeSync(String query, String category) {
    try {
      AppContext.getLayoutController().syncHeaderSearch(query, category);
    } catch (IllegalStateException ignored) {
      // Layout not yet initialised (shouldn't happen in normal flow)
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void setLoading(boolean l) {
    loadingPane.setVisible(l);
    loadingPane.setManaged(l);
  }

  private void hideError() {
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
  }

  private String getStatusClass(String status) {
    if (status == null) return "status-pending";
    return switch (status.toUpperCase()) {
      case "ACTIVE"    -> "status-active";
      case "PENDING"   -> "status-pending";
      case "CLOSED"    -> "status-closed";
      case "CANCELLED" -> "status-cancelled";
      default          -> "status-pending";
    };
  }

  private String formatTimeInfo(AuctionResponse a) {
    LocalDateTime now = LocalDateTime.now();
    if (a.endTime() != null && a.endTime().isBefore(now))
      return "Ended " + a.endTime().format(TIME_FMT);
    if (a.startTime() != null && a.startTime().isAfter(now))
      return "Starts " + a.startTime().format(TIME_FMT);
    if (a.endTime() != null) {
      long h = ChronoUnit.HOURS.between(now, a.endTime());
      return h < 24 ? "Ends in " + h + "h" : "Ends " + a.endTime().format(TIME_FMT);
    }
    return "";
  }
}
