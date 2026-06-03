package org.tamtamcatworks.auction.client.auth.admin;

import java.util.Set;

import org.tamtamcatworks.auction.client.SessionManager;

public final class AdminAuthorizationService {

    private AdminAuthorizationService() {}

    public static boolean hasPermission(
            AdminPermission permission
    ) {

        var user =
                SessionManager.getCurrentUser();

        if (user == null) {

            return false;
        }

        if (user.isAdmin()) {

            return true;
        }

        return user.permissions()
                .contains(
                        permission.name()
                );
    }

    public static boolean hasRole(
            AdminRole role
    ) {

        var user =
                SessionManager.getCurrentUser();

        if (user == null) {

            return false;
        }

        return user.permissions()
                .contains(
                        role.name()
                );
    }

    public static Set<String> getPermissions() {

        var user =
                SessionManager.getCurrentUser();

        if (user == null) {

            return Set.of();
        }

        return Set.copyOf(
                user.permissions()
        );
    }
}