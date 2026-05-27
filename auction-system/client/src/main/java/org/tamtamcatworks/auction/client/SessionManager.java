package org.tamtamcatworks.auction.client;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tamtamcatworks.auction.shared.response.UserResponse;


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

}
