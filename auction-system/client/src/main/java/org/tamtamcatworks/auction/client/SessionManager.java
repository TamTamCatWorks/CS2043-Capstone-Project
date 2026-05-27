package org.tamtamcatworks.auction.client;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tamtamcatworks.auction.shared.response.UserResponse;
import org.tamtamcatworks.auction.client.ws.AuctionWebSocketClient;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Application-wide session state.
 *
 * <p>{@code currentUser} is exposed as a JavaFX {@link ObjectProperty} so that
 * any controller can react to changes (e.g. balance updates after a top-up)
 * without needing {@code scene.lookup()} hacks.
 *
 * <pre>{@code
 * // Listen for user changes in any controller:
 * SessionManager.currentUserProperty()
 *     .addListener((obs, old, user) -> updateBalanceLabels(user));
 *
 * // Writing still uses the same familiar call:
 * SessionManager.setCurrentUser(updatedUser);
 * }</pre>
 */
public class SessionManager {

  private static final ObjectProperty<UserResponse> currentUserProp =
      new SimpleObjectProperty<>(null);
  private static final ApiClient apiClient = new ApiClient();

  // Shared WebSocket client for the logged-in session. Use the helper
  // methods below to subscribe/unsubscribe so the app keeps a single
  // connection per session.
  private static AuctionWebSocketClient webSocketClient = null;
  private static CompletableFuture<AuctionWebSocketClient> webSocketClientFuture = null;
  private static final List<StompSession.Subscription> activeSubscriptions = new ArrayList<>();

  // ── API client ─────────────────────────────────────────────────────────────

  /** Returns the shared {@link ApiClient} instance. */
  public static ApiClient getApiClient() {
    return apiClient;
  }

  // ── Current user ───────────────────────────────────────────────────────────

  /**
   * Observable property holding the currently logged-in user, or {@code null}
   * when no one is logged in. Bind to this to react automatically to changes.
   */
  public static ObjectProperty<UserResponse> currentUserProperty() {
    return currentUserProp;
  }

  /** Returns the currently logged-in user, or {@code null}. */
  public static UserResponse getCurrentUser() {
    return currentUserProp.get();
  }

  /**
   * Set the current user. Any listeners on {@link #currentUserProperty()} are
   * notified immediately on the calling thread (should be the FX thread).
   */
  public static void setCurrentUser(UserResponse user) {
    currentUserProp.set(user);
    if (user != null) {
      // Establish shared WebSocket connection once for this session.
      ensureWebSocketConnected().exceptionally(ex -> {
        System.err.println("WebSocket connect failed: " + (ex != null ? ex.getMessage() : "?"));
        return null;
      });
    }
  }

  /** Returns {@code true} when a user is logged in. */
  public static boolean isLoggedIn() {
    return currentUserProp.get() != null;
  }

  /**
   * Clear the current user session (logout). Notifies any property listeners.
   */
  public static void logout() {
    // Tear down any session-level websocket subscriptions and connection
    // before clearing the user so no callbacks remain active after logout.
    disconnectWebSocket();
    currentUserProp.set(null);
  }

  // --- WebSocket helpers (session-scoped) ---------------------------------

  private static synchronized CompletableFuture<AuctionWebSocketClient> ensureWebSocketConnected() {
    if (webSocketClient == null) {
      webSocketClient = new AuctionWebSocketClient();
    }

    if (webSocketClientFuture == null) {
      webSocketClientFuture = webSocketClient.connect().thenApply(s -> webSocketClient);
    }

    return webSocketClientFuture;
  }

  public static CompletableFuture<StompSession.Subscription> subscribeToPrice(String auctionId,
      Consumer<AuctionWebSocketClient.AuctionPriceUpdate> onPriceUpdate) {
    return ensureWebSocketConnected().thenApply(ws -> {
      StompSession.Subscription sub = ws.subscribeToPrice(auctionId, onPriceUpdate);
      if (sub != null) synchronized (activeSubscriptions) { activeSubscriptions.add(sub); }
      return sub;
    });
  }

  public static CompletableFuture<StompSession.Subscription> subscribeToStatus(String auctionId,
      Consumer<AuctionWebSocketClient.AuctionStatusUpdate> onStatusUpdate) {
    return ensureWebSocketConnected().thenApply(ws -> {
      StompSession.Subscription sub = ws.subscribeToStatus(auctionId, onStatusUpdate);
      if (sub != null) synchronized (activeSubscriptions) { activeSubscriptions.add(sub); }
      return sub;
    });
  }

  public static CompletableFuture<StompSession.Subscription> subscribeToNotifications(
      Consumer<org.tamtamcatworks.auction.shared.response.NotificationResponse> onNotification) {
    return ensureWebSocketConnected().thenApply(ws -> {
      StompSession.Subscription sub = ws.subscribeToNotifications(onNotification);
      if (sub != null) synchronized (activeSubscriptions) { activeSubscriptions.add(sub); }
      return sub;
    });
  }

  public static synchronized void unsubscribe(StompSession.Subscription sub) {
    if (sub == null) return;
    try {
      sub.unsubscribe();
    } catch (Exception ignored) {
    }
    activeSubscriptions.remove(sub);
  }

  private static synchronized void disconnectWebSocket() {
    // Unsubscribe any remaining subscriptions, then disconnect the client.
    for (StompSession.Subscription s : List.copyOf(activeSubscriptions)) {
      try { s.unsubscribe(); } catch (Exception ignored) {}
    }
    activeSubscriptions.clear();

    if (webSocketClient != null) {
      try { webSocketClient.disconnect(); } catch (Exception ignored) {}
      webSocketClient = null;
    }
    webSocketClientFuture = null;
  }

}
