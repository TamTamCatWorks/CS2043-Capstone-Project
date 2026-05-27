package org.tamtamcatworks.auction.client.controller.shell;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.ArrayList;
import java.util.List;

final class NotificationMenuManager {

    private final MenuButton userMenuButton;
    private final MenuItem notificationsHeaderItem;
    private final SeparatorMenuItem notificationsSeparatorItem;
    private final Label unreadBadgeLabel = new Label();
    private final List<MenuItem> renderedNotificationItems = new ArrayList<>();
    private final List<NotificationResponse> currentNotifications = new ArrayList<>();

    private StompSession.Subscription notificationSubscription;

    NotificationMenuManager(MenuButton userMenuButton,
                            MenuItem notificationsHeaderItem,
                            SeparatorMenuItem notificationsSeparatorItem) {
        this.userMenuButton = userMenuButton;
        this.notificationsHeaderItem = notificationsHeaderItem;
        this.notificationsSeparatorItem = notificationsSeparatorItem;
        this.notificationsHeaderItem.setGraphic(buildHeaderGraphic());
    }

    void start() {
        refreshNotificationMenu(List.of());
        startNotificationWebSocket();
    }

    void stop() {
        if (notificationSubscription != null) {
            SessionManager.unsubscribe(notificationSubscription);
            notificationSubscription = null;
        }
    }

    void handleMenuOpened() {
        new Thread(() -> {
            try {
                SessionManager.getApiClient().markNotificationsRead();
            } catch (Exception ignored) {
                // API may not be ready yet.
            }
            Platform.runLater(() -> {
                currentNotifications.replaceAll(notification -> new NotificationResponse(
                    notification.id(),
                    notification.type(),
                    notification.message(),
                    true,
                    notification.createdAt()
                ));
                refreshNotificationMenu(List.copyOf(currentNotifications));
            });
        }, "mark-notifs-read").start();
    }

    private void startNotificationWebSocket() {
        SessionManager.subscribeToNotifications(notification ->
            Platform.runLater(() -> onNotificationReceived(notification))
        ).thenAccept(sub -> notificationSubscription = sub)
          .exceptionally(ex -> { System.err.println("Notification WebSocket failed: " + (ex != null ? ex.getMessage() : "?")); return null; });
    }

    private void onNotificationReceived(NotificationResponse notification) {
        currentNotifications.removeIf(existing -> existing.id().equals(notification.id()));
        currentNotifications.add(0, notification);
        refreshNotificationMenu(List.copyOf(currentNotifications));
    }

    private void refreshNotificationMenu(List<NotificationResponse> notifications) {
        currentNotifications.clear();
        if (notifications != null) {
            currentNotifications.addAll(notifications);
        }

        userMenuButton.getItems().removeAll(renderedNotificationItems);
        renderedNotificationItems.clear();

        if (notifications == null || notifications.isEmpty()) {
            MenuItem empty = new MenuItem("No notifications");
            empty.setDisable(true);
            empty.setGraphic(new FontIcon("mdi2b-bell-sleep-outline"));
            renderedNotificationItems.add(empty);
            insertRenderedItems();
            updateUnreadBadge(0);
            return;
        }

        long unreadCount = notifications.stream().filter(n -> !n.read()).count();
        updateUnreadBadge(unreadCount);

        int limit = Math.min(notifications.size(), 10);
        for (int i = 0; i < limit; i++) {
            NotificationResponse notification = notifications.get(i);
            MenuItem item = new MenuItem(notification.message());
            item.setGraphic(buildNotificationIcon(notification.type()));
            if (notification.read()) {
                item.setStyle("-fx-opacity: 0.6;");
            }
            renderedNotificationItems.add(item);
        }

        insertRenderedItems();
    }

    private void insertRenderedItems() {
        int separatorIndex = userMenuButton.getItems().indexOf(notificationsSeparatorItem);
        if (separatorIndex < 0) {
            userMenuButton.getItems().addAll(renderedNotificationItems);
            return;
        }

        userMenuButton.getItems().addAll(separatorIndex, renderedNotificationItems);
    }

    private void updateUnreadBadge(long unreadCount) {
        if (unreadCount > 0) {
            unreadBadgeLabel.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
            unreadBadgeLabel.setVisible(true);
            unreadBadgeLabel.setManaged(true);
        } else {
            unreadBadgeLabel.setVisible(false);
            unreadBadgeLabel.setManaged(false);
        }
    }

    private HBox buildHeaderGraphic() {
        FontIcon icon = new FontIcon("mdi2b-bell-outline");
        icon.setIconSize(16);

        unreadBadgeLabel.getStyleClass().add("user-menu-notification-badge");
        unreadBadgeLabel.setVisible(false);
        unreadBadgeLabel.setManaged(false);

        HBox graphic = new HBox(8, icon, unreadBadgeLabel);
        graphic.getStyleClass().add("user-menu-notification-header");
        return graphic;
    }

    private FontIcon buildNotificationIcon(String type) {
        String iconCode = switch (type) {
            case "BID_PLACED" -> "mdi2c-cash-plus";
            case "OUTBID" -> "mdi2a-alert-circle-outline";
            case "AUCTION_OPENED" -> "mdi2p-play-circle-outline";
            case "AUCTION_CLOSED" -> "mdi2c-check-circle-outline";
            case "AUCTION_CANCELLED" -> "mdi2c-close-circle-outline";
            default -> "mdi2b-bell-outline";
        };
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        return icon;
    }
}