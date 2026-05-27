package org.tamtamcatworks.auction.client.controller.shell;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import org.tamtamcatworks.auction.client.AsyncTask;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.NavigationState;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.client.ViewLoader;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.List;

/**
 * Controller for the profile-style dashboard.
 *
 * <p>Left column: profile card with name, balance and navigation.
 * Right column: dynamic content area loaded via {@link ViewLoader}.
 *
 * <p>Balance labels update automatically whenever
 * {@link SessionManager#setCurrentUser(UserResponse)} is called from any
 * controller (e.g. after a top-up), via a listener on the reactive
 * {@link SessionManager#currentUserProperty()}.
 */
@Route(fxml = "/fxml/dashboard.fxml", layout = Route.DASHBOARD_LAYOUT)
public class DashboardController {

  // Left column profile
  @FXML private Label avatarInitialsLabel;
  @FXML private Label fullNameLabel;
  @FXML private Label usernameLabel;
  @FXML private Label emailLabel;
  @FXML private Label totalBalanceLabel;
  @FXML private Label holdBalanceLabel;
  @FXML private Label availableBalanceLabel;
  @FXML private ProgressBar balanceProgressBar;
  @FXML private Label activePercentLabel;
  @FXML private Label auctionCountLabel;
  @FXML private Label bidCountLabel;
  @FXML private Label wonCountLabel;
  @FXML private Label memberSinceLabel;

  @FXML private Button menuHomeButton;
  @FXML private Button menuNotificationsButton;
  @FXML private Button menuTopUpButton;

  // Dynamic right content area
  @FXML private StackPane rightContentArea;

  private TabChipBar menuTabs;

  @FXML
  public void initialize() {
    if (!SessionManager.isLoggedIn()) {
      Platform.runLater(() -> Navigation.navigateTo("/fxml/login.fxml"));
      return;
    }

    menuTabs = new TabChipBar("profile-tab-chip-selected",
        menuHomeButton, menuNotificationsButton, menuTopUpButton);

    UserResponse user = SessionManager.getCurrentUser();
    populateProfile(user);

    // React to user updates from any source, including websocket-driven state changes.
    SessionManager.currentUserProperty().addListener((obs, old, updated) -> {
      if (updated != null) {
        populateProfile(updated);
      }
    });

    // Wire left menu buttons
    menuHomeButton.setOnAction(e -> handleMenuHome());
    menuNotificationsButton.setOnAction(e -> handleMenuNotifications());
    menuTopUpButton.setOnAction(e -> handleMenuTopUp());

    // Honor pending dashboard view request (e.g. from notification menu)
    String pending = NavigationState.getDashboardViewPath();
    if (pending != null && !pending.isBlank()) {
      Button preselect = pending.endsWith("notifications.fxml") ? menuNotificationsButton
          : pending.endsWith("topup.fxml") ? menuTopUpButton
          : menuHomeButton;
      menuTabs.select(preselect);
      ViewLoader.into(rightContentArea).load(pending);
      NavigationState.setDashboardViewPath(null);
    } else {
      menuTabs.select(menuHomeButton);
      ViewLoader.into(rightContentArea).load("/fxml/dashboard/home.fxml");
    }

    loadCounts(user);
  }

  @FXML
  private void handleMenuHome() {
    menuTabs.select(menuHomeButton);
    ViewLoader.into(rightContentArea).load("/fxml/dashboard/home.fxml");
  }

  @FXML
  private void handleMenuNotifications() {
    menuTabs.select(menuNotificationsButton);
    ViewLoader.into(rightContentArea).load("/fxml/dashboard/notifications.fxml");
  }

  @FXML
  private void handleMenuTopUp() {
    menuTabs.select(menuTopUpButton);
    ViewLoader.into(rightContentArea).load("/fxml/dashboard/topup.fxml");
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private void populateProfile(UserResponse user) {
    if (user == null) {
      return;
    }
    avatarInitialsLabel.setText(buildInitials(user.fullName()));
    fullNameLabel.setText(user.fullName());
    usernameLabel.setText("@" + user.username());
    emailLabel.setText(user.email());
    updateBalanceLabels(user);
    memberSinceLabel.setText("Member since 2026");
  }

  private void loadCounts(UserResponse user) {
    AsyncTask.<List<AuctionResponse>>run(SessionManager.getApiClient()::getAllAuctions)
        .onSuccess(all -> {
          int auctions = (int) all.stream()
              .filter(a -> user.id().equals(a.sellerId())).count();
          int bids = (int) all.stream().flatMap(a -> {
            try {
              return SessionManager.getApiClient().getBids(a.id()).stream();
            } catch (Exception ex) {
              return java.util.stream.Stream.empty();
            }
          }).filter(b -> user.id().equals(b.bidderId())).count();
          int won = (int) all.stream()
              .filter(a -> "CLOSED".equals(a.status())
                  && user.id().equals(a.leadingBidderId())).count();
          auctionCountLabel.setText(String.valueOf(auctions));
          bidCountLabel.setText(String.valueOf(bids));
          wonCountLabel.setText(String.valueOf(won));
        })
        .start();
  }

  private void updateBalanceLabels(UserResponse user) {
    if (user == null) {
      return;
    }
    double available = user.balance();
    double hold = user.holdBalance();
    double total = available + hold;
    totalBalanceLabel.setText(String.format("Total: $%,.2f", total));
    holdBalanceLabel.setText(String.format("On Hold: $%,.2f (%.0f%%)",
        hold, total == 0 ? 0.0 : (hold / total) * 100.0));
    availableBalanceLabel.setText(String.format("$%,.2f", available));
    double progress = (total == 0) ? 0.0 : (available / total);
    balanceProgressBar.setProgress(progress);
    activePercentLabel.setText(String.format("Active Funds (%.0f%%)", progress * 100.0));
  }

  private String buildInitials(String fullName) {
    if (fullName == null || fullName.isBlank()) {
      return "?";
    }
    String[] parts = fullName.trim().split("\\s+");
    if (parts.length == 1) {
      return parts[0].substring(0, 1).toUpperCase();
    }
    return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
  }
}
