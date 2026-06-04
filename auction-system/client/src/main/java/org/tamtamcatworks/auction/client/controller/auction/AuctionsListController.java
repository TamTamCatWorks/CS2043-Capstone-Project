package org.tamtamcatworks.auction.client.controller.auction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.AppContext;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SearchParams;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

/**
 * Controller for the Browse Auctions page — horizontal shelf (Netflix-row) layout.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Fetch all auctions once and distribute them across 8 named shelf rows.</li>
 *   <li>Delegate "See all →" clicks to
 *       {@link org.tamtamcatworks.auction.client.controller.shell.LayoutController#navigateToSearchResults}
 *       so the global header stays in sync and the correct SearchParams are
 *       forwarded to the search-results page.</li>
 * </ul>
 *
 * <h3>No search bar</h3>
 * <p>This controller owns <em>no</em> search field.  The global header search
 * in {@code dashboard-layout.fxml} is the single search entry point.
 */
@Route(fxml = "/fxml/auctions-list.fxml", layout = Route.DASHBOARD_LAYOUT)
public class AuctionsListController {

  // ── FXML injected fields ─────────────────────────────────────────────────

  @FXML private Label      errorLabel;

  // Discovery shelf rows
  @FXML private HBox       featuredRow;
  @FXML private HBox       endingSoonRow;
  @FXML private HBox       newArrivalsRow;
  @FXML private HBox       popularRow;

  // Category shelf rows
  @FXML private HBox       artRow;
  @FXML private HBox       electronicsRow;
  @FXML private HBox       vehiclesRow;
  @FXML private HBox       otherRow;

  // Per-shelf overlays
  @FXML private StackPane  featuredOverlay;
  @FXML private StackPane  endingSoonOverlay;
  @FXML private StackPane  newArrivalsOverlay;
  @FXML private StackPane  popularOverlay;
  @FXML private StackPane  artOverlay;
  @FXML private StackPane  electronicsOverlay;
  @FXML private StackPane  vehiclesOverlay;
  @FXML private StackPane  otherOverlay;

  // ── Internal state ───────────────────────────────────────────────────────

  private final List<AuctionResponse> allAuctions = new ArrayList<>();

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    loadAllAuctions();
  }

  // ── Data loading ──────────────────────────────────────────────────────────

  private void loadAllAuctions() {
    showAllOverlays(true);
    clearAllRows();

    AsyncTask.<List<AuctionResponse>>run(() -> SessionManager.getApiClient().getAllAuctions())
        .onSuccess(auctions -> {
          allAuctions.clear();
          if (auctions != null) allAuctions.addAll(auctions);
          renderShelves(allAuctions);
          showAllOverlays(false);
        })
        .onFailure(ex -> {
          showAllOverlays(false);
          String msg = ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error";
          errorLabel.setText("Failed to load auctions: " + msg);
          errorLabel.setVisible(true);
          errorLabel.setManaged(true);
          showEmptyOnAllShelves();
        })
        .start();
  }

  // ── Filtering ─────────────────────────────────────────────────────────────

  // ── Shelf rendering ───────────────────────────────────────────────────────

  private void renderShelves(List<AuctionResponse> source) {
    List<AuctionResponse> featured = source.stream()
        .filter(a -> "ACTIVE".equalsIgnoreCase(a.status()))
        .sorted(Comparator.comparingDouble(AuctionResponse::currentPrice).reversed())
        .limit(12).collect(Collectors.toList());

    List<AuctionResponse> endingSoon = source.stream()
        .filter(this::isEndingSoon)
        .sorted(Comparator.comparing(a -> a.endTime() != null ? a.endTime() : LocalDateTime.MAX))
        .limit(12).collect(Collectors.toList());

    List<AuctionResponse> newArrivals = source.stream()
        .filter(this::isNew)
        .sorted(Comparator.comparing((AuctionResponse a) ->
                a.startTime() != null ? a.startTime() : LocalDateTime.MIN).reversed())
        .limit(12).collect(Collectors.toList());

    List<AuctionResponse> popular = source.stream()
        .filter(a -> "ACTIVE".equalsIgnoreCase(a.status()))
        .sorted(Comparator.comparingDouble((AuctionResponse a) -> a.currentPrice()).reversed())
        .limit(12).collect(Collectors.toList());

    List<AuctionResponse> art = source.stream()
        .filter(a -> "ART".equalsIgnoreCase(a.itemType()))
        .limit(12).collect(Collectors.toList());

    List<AuctionResponse> electronics = source.stream()
        .filter(a -> "ELECTRONICS".equalsIgnoreCase(a.itemType()))
        .limit(12).collect(Collectors.toList());

    List<AuctionResponse> vehicles = source.stream()
        .filter(a -> "VEHICLE".equalsIgnoreCase(a.itemType()))
        .limit(12).collect(Collectors.toList());

    List<AuctionResponse> other = source.stream()
        .filter(a -> a.itemType() == null
            || (!a.itemType().equalsIgnoreCase("ART")
                && !a.itemType().equalsIgnoreCase("ELECTRONICS")
                && !a.itemType().equalsIgnoreCase("VEHICLE")))
        .limit(12).collect(Collectors.toList());

    populateShelf(featuredRow,    featuredOverlay,    featured);
    populateShelf(endingSoonRow,  endingSoonOverlay,  endingSoon);
    populateShelf(newArrivalsRow, newArrivalsOverlay, newArrivals);
    populateShelf(popularRow,     popularOverlay,     popular);
    populateShelf(artRow,         artOverlay,         art);
    populateShelf(electronicsRow, electronicsOverlay, electronics);
    populateShelf(vehiclesRow,    vehiclesOverlay,    vehicles);
    populateShelf(otherRow,       otherOverlay,       other);
  }

  private void populateShelf(HBox row, StackPane overlay, List<AuctionResponse> items) {
    row.getChildren().clear();
    overlay.getChildren().clear();

    if (items.isEmpty()) {
      Label empty = new Label("Nothing here yet");
      empty.getStyleClass().add("shelf-empty-label");
      VBox wrap = new VBox(empty);
      wrap.setAlignment(Pos.CENTER);
      overlay.getChildren().add(wrap);
      overlay.setVisible(true);
      overlay.setManaged(true);
    } else {
      overlay.setVisible(false);
      overlay.setManaged(false);
      for (AuctionResponse a : items) {
        row.getChildren().add(AuctionCardFactory.createAuctionCard(a));
      }
    }
  }

  // ── Overlay helpers ───────────────────────────────────────────────────────

  private void showAllOverlays(boolean loading) {
    for (StackPane overlay : allOverlays()) {
      overlay.getChildren().clear();
      if (loading) {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxWidth(32);
        pi.setMaxHeight(32);
        pi.getStyleClass().add("progress-spinner");
        VBox wrap = new VBox(pi);
        wrap.setAlignment(Pos.CENTER);
        overlay.getChildren().add(wrap);
        overlay.setVisible(true);
        overlay.setManaged(true);
      } else {
        overlay.setVisible(false);
        overlay.setManaged(false);
      }
    }
  }

  private void clearAllRows() {
    List.of(featuredRow, endingSoonRow, newArrivalsRow, popularRow,
            artRow, electronicsRow, vehiclesRow, otherRow)
        .forEach(r -> r.getChildren().clear());
  }

  private void showEmptyOnAllShelves() {
    for (StackPane overlay : allOverlays()) {
      overlay.getChildren().clear();
      Label empty = new Label("Nothing here yet");
      empty.getStyleClass().add("shelf-empty-label");
      VBox wrap = new VBox(empty);
      wrap.setAlignment(Pos.CENTER);
      overlay.getChildren().add(wrap);
      overlay.setVisible(true);
      overlay.setManaged(true);
    }
  }

  private List<StackPane> allOverlays() {
    return List.of(featuredOverlay, endingSoonOverlay, newArrivalsOverlay, popularOverlay,
                   artOverlay, electronicsOverlay, vehiclesOverlay, otherOverlay);
  }

  // ── Time helpers ──────────────────────────────────────────────────────────

  private boolean isEndingSoon(AuctionResponse a) {
    if (a.endTime() == null || !"ACTIVE".equalsIgnoreCase(a.status())) return false;
    LocalDateTime soon = LocalDateTime.now().plusHours(24);
    return a.endTime().isAfter(LocalDateTime.now()) && a.endTime().isBefore(soon);
  }

  private boolean isNew(AuctionResponse a) {
    if (a.startTime() == null) return false;
    return a.startTime().isAfter(LocalDateTime.now().minusDays(3));
  }

  // ── "See all" handlers — delegate to LayoutController ────────────────────

  @FXML private void onSeeAllFeatured()    { navigateTo(SearchParams.forStatus("ACTIVE")); }
  @FXML private void onSeeAllEndingSoon()  { navigateTo(new SearchParams("", "All categories", "ACTIVE", "Ending Soon", 0)); }
  @FXML private void onSeeAllNew()         { navigateTo(new SearchParams("", "All categories", "ALL", "Newest First", 0)); }
  @FXML private void onSeeAllPopular()     { navigateTo(new SearchParams("", "All categories", "ACTIVE", "Most Bids", 0)); }
  @FXML private void onSeeAllArt()         { navigateTo(SearchParams.forCategory("Art")); }
  @FXML private void onSeeAllElectronics() { navigateTo(SearchParams.forCategory("Electronics")); }
  @FXML private void onSeeAllVehicles()    { navigateTo(SearchParams.forCategory("Vehicle")); }
  @FXML private void onSeeAllOther()       { navigateTo(SearchParams.forCategory("Other")); }

  private void navigateTo(SearchParams params) {
    AppContext.getLayoutController().navigateToSearchResults(params);
  }
}
