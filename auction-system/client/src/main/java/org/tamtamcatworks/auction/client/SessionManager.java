package org.tamtamcatworks.auction.client;

import org.tamtamcatworks.auction.shared.response.UserResponse;

public class SessionManager {
    private static UserResponse currentUser;
    private static final ApiClient apiClient = new ApiClient();

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
}
