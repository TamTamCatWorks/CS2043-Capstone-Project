package org.tamtamcatworks.auction.client;

import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionManager {
    private static UserResponse currentUser;
    private static final ApiClient apiClient = new ApiClient();
    // Dashboard tab index requested by navigation (0-based)
    private static int dashboardTabIndex = 0;
    private static String dashboardViewPath = null;
    private static final ArrayDeque<String> recentSearches = new ArrayDeque<>();
    private static String pendingSearchQuery = null;
    private static String pendingSearchCategory = null;

    public static ApiClient getApiClient() {
        return apiClient;
    }

    public static UserResponse getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserResponse user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
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
