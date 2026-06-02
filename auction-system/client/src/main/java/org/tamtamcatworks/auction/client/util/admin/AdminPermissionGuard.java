package org.tamtamcatworks.auction.client.util.admin;

import org.tamtamcatworks.auction.client.SessionManager;

public final class AdminPermissionGuard {

    private AdminPermissionGuard() {}

    public static boolean hasPermission(
            AdminPermission permission
    ) {

        var user =
                SessionManager.getCurrentUser();

        if (user == null) {
            return false;
        }

        if (!user.isAdmin()) {
            return false;
        }

        return user.permissions()
                .contains(permission.name());
    }

    public static void requireAdmin() {

        var user =
                SessionManager.getCurrentUser();

        if (user == null || !user.isAdmin()) {

            throw new RuntimeException(
                    "Admin access denied"
            );
        }
    }
}