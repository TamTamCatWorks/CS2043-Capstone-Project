package org.tamtamcatworks.auction.client.controller.shell;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import org.tamtamcatworks.auction.client.ApiClient;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.List;

/**
 * Controller for the profile-style dashboard.
 * Left column holds profile and navigation; right column is a dynamic content area
 * that loads modular FXML views (auctions, bids, notifications).
 */
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

    private final ApiClient apiClient = SessionManager.getApiClient();

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn()) {
            Platform.runLater(() -> Navigation.navigateTo("/fxml/login.fxml"));
            return;
        }

        UserResponse user = SessionManager.getCurrentUser();

        String initials = buildInitials(user.fullName());
        avatarInitialsLabel.setText(initials);
        fullNameLabel.setText(user.fullName());
        usernameLabel.setText("@" + user.username());
        emailLabel.setText(user.email());
        updateBalanceLabels(user);
        memberSinceLabel.setText("Member since 2026");

        // wire left menu buttons (FXML already references handlers but keep safe)
        menuHomeButton.setOnAction(e -> handleMenuHome());
        menuNotificationsButton.setOnAction(e -> handleMenuNotifications());
        menuTopUpButton.setOnAction(e -> handleMenuTopUp());
        setOuterSelected(menuHomeButton);

        // load initial view (honor any pending request from SessionManager)
        String pending = SessionManager.getDashboardViewPath();
        if (pending != null && !pending.isBlank()) {
            if (pending.endsWith("notifications.fxml")) {
                setOuterSelected(menuNotificationsButton);
            } else if (pending.endsWith("topup.fxml")) {
                setOuterSelected(menuTopUpButton);
            } else {
                setOuterSelected(menuHomeButton);
            }
            loadView(pending);
            SessionManager.setDashboardViewPath(null);
        } else {
            setOuterSelected(menuHomeButton);
            loadView("/fxml/dashboard/home.fxml");
        }

        // compute counts asynchronously
        loadCounts(user);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            rightContentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleMenuHome() {
        setOuterSelected(menuHomeButton);
        loadView("/fxml/dashboard/home.fxml");
    }

    @FXML
    private void handleMenuNotifications() {
        setOuterSelected(menuNotificationsButton);
        loadView("/fxml/dashboard/notifications.fxml");
    }

    @FXML
    private void handleMenuTopUp() {
        setOuterSelected(menuTopUpButton);
        loadView("/fxml/dashboard/topup.fxml");
    }

    private void setOuterSelected(Button selectedButton) {
        applySelected(menuHomeButton, menuHomeButton == selectedButton);
        applySelected(menuNotificationsButton, menuNotificationsButton == selectedButton);
        applySelected(menuTopUpButton, menuTopUpButton == selectedButton);
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

    private void loadCounts(UserResponse user) {
        Task<Void> task = new Task<>() {
            int auctions = 0, bids = 0, won = 0;
            @Override
            protected Void call() {
                List<AuctionResponse> all = apiClient.getAllAuctions();
                auctions = (int) all.stream().filter(a -> user.id().equals(a.sellerId())).count();
                bids = (int) all.stream().flatMap(a -> {
                    try { return apiClient.getBids(a.id()).stream(); } catch (Exception ex) { return java.util.stream.Stream.empty(); }
                }).filter(b -> user.id().equals(b.bidderId())).count();
                won = (int) all.stream().filter(a -> "CLOSED".equals(a.status()) && user.id().equals(a.leadingBidderId())).count();
                return null;
            }
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    auctionCountLabel.setText(String.valueOf(auctions));
                    bidCountLabel.setText(String.valueOf(bids));
                    wonCountLabel.setText(String.valueOf(won));
                });
            }
        };
        new Thread(task, "load-dashboard-counts").start();
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void updateBalanceLabels(UserResponse user) {
        if (user == null) {
            return;
        }
        double available = user.balance();
        double hold = user.holdBalance();
        double total = available + hold;
        // update textual values
        totalBalanceLabel.setText(String.format("Total: $%,.2f", total));
        holdBalanceLabel.setText(String.format("On Hold: $%,.2f (%.0f%%)", hold, total == 0 ? 0.0 : (hold / total) * 100.0));
        availableBalanceLabel.setText(String.format("$%,.2f", available));

        // update progress bar and percent label (available / total)
        double progress = (total == 0) ? 0.0 : (available / total);
        balanceProgressBar.setProgress(progress);
        double activePct = (total == 0) ? 0.0 : (progress * 100.0);
        activePercentLabel.setText(String.format("Active Funds (%.0f%%)", activePct));
    }
}
