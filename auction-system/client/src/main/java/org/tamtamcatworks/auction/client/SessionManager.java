package org.tamtamcatworks.auction.client;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

  // Dashboard tab index requested by navigation (0-based)
  private static int dashboardTabIndex = 0;
  private static String dashboardViewPath = null;
  private static final ArrayDeque<String> recentSearches = new ArrayDeque<>();
  private static String pendingSearchQuery = null;
  private static String pendingSearchCategory = null;

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
  }

  /** Returns {@code true} when a user is logged in. */
  public static boolean isLoggedIn() {
    return currentUserProp.get() != null;
  }

  /**
   * Clear the current user session (logout). Notifies any property listeners.
   */
  public static void logout() {
    currentUserProp.set(null);
  }

  // ── Dashboard sub-view routing ─────────────────────────────────────────────

  public static void setDashboardTabIndex(int idx) {
    dashboardTabIndex = idx;
  }

  public static int getDashboardTabIndex() {
    return dashboardTabIndex;
  }

  public static void setDashboardViewPath(String path) {
    dashboardViewPath = path;
  }

  public static String getDashboardViewPath() {
    return dashboardViewPath;
  }

  // ── Search state ───────────────────────────────────────────────────────────

  /** Add a search term to the recent-searches ring buffer (max 5 entries). */
  public static void addRecentSearch(String query) {
    if (query == null) {
      return;
    }
    String normalized = query.trim();
    if (normalized.isEmpty()) {
      return;
    }
    recentSearches.removeIf(existing -> existing.equalsIgnoreCase(normalized));
    recentSearches.addFirst(normalized);
    while (recentSearches.size() > 5) {
      recentSearches.removeLast();
    }
  }

  public static List<String> getRecentSearches() {
    return Collections.unmodifiableList(new ArrayList<>(recentSearches));
  }

  public static void setPendingSearch(String query, String category) {
    pendingSearchQuery = query;
    pendingSearchCategory = category;
  }

  public static String getPendingSearchQuery() {
    return pendingSearchQuery;
  }

  public static String getPendingSearchCategory() {
    return pendingSearchCategory;
  }

  public static void clearPendingSearch() {
    pendingSearchQuery = null;
    pendingSearchCategory = null;
  }
}
