package org.tamtamcatworks.auction.client;

import org.tamtamcatworks.auction.shared.response.UserResponse;

public class SessionManager {
    private static UserResponse currentUser;
    private static final ApiClient apiClient = new ApiClient();
    // Dashboard tab index requested by navigation (0-based)
    private static int dashboardTabIndex = 0;
    private static String dashboardViewPath = null;

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
}
