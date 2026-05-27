package org.tamtamcatworks.auction.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Shared navigation and search state for dashboard-driven flows. */
public final class NavigationState {

  private static int dashboardTabIndex = 0;
  private static String dashboardViewPath = null;
  private static final ArrayDeque<String> recentSearches = new ArrayDeque<>();
  private static String pendingSearchQuery = null;
  private static String pendingSearchCategory = null;

  private NavigationState() {
  }

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