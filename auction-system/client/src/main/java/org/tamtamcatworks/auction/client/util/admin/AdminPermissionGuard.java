package org.tamtamcatworks.auction.client.util.admin;

import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;

public final class AdminPermissionGuard {

    private static final String UNAUTHORIZED_PAGE =
            "/fxml/unauthorized.fxml";

    private AdminPermissionGuard() {}

    public static void requireAdmin() {

        if (!SessionManager.isAdmin()) {

            Navigation.navigateTo(
                    UNAUTHORIZED_PAGE
            );

            throw new IllegalStateException(
                    "Admin permission required"
            );
        }
    }

    public static void requirePermission(
            String permission
    ) {

        if (!SessionManager.hasPermission(permission)) {

            Navigation.navigateTo(
                    UNAUTHORIZED_PAGE
            );

            throw new IllegalStateException(
                    "Permission denied: " + permission
            );
        }
    }
}