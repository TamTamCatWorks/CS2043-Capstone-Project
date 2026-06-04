package org.tamtamcatworks.auction.client.auth.admin;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAuthorizationServiceTest {

    @BeforeEach
    void clearSession() {

        SessionManager.currentUserProperty().set(null);
    }

    @AfterEach
    void resetSession() {

        SessionManager.currentUserProperty().set(null);
    }

    @Test
    void noUserHasNoAdminAccess() {

        assertFalse(AdminAuthorizationService.hasPermission(AdminPermission.USER_MANAGE));
        assertFalse(AdminAuthorizationService.hasRole(AdminRole.SUPER_ADMIN));
        assertEquals(Set.of(), AdminAuthorizationService.getPermissions());
    }

    @Test
    void adminUserHasAllPermissions() {

        SessionManager.currentUserProperty().set(
                new UserResponse(
                        "admin-1",
                        "admin",
                        "admin@example.com",
                        "Admin User",
                        100.0,
                        0.0,
                        true,
                        true,
                        List.of("AUDIT_VIEW", "MODERATOR")
                )
        );

        assertTrue(AdminAuthorizationService.hasPermission(AdminPermission.AUCTION_MANAGE));
                assertTrue(AdminAuthorizationService.hasRole(AdminRole.MODERATOR));
                assertEquals(Set.of("AUDIT_VIEW", "MODERATOR"), AdminAuthorizationService.getPermissions());
    }

    @Test
    void permissionAndRoleChecksUseExplicitClaimsForNonAdmins() {

        SessionManager.currentUserProperty().set(
                new UserResponse(
                        "user-1",
                        "moderator",
                        "moderator@example.com",
                        "Moderation User",
                        50.0,
                        0.0,
                        true,
                        false,
                        List.of("AUCTION_MANAGE", "SUPPORT_ADMIN")
                )
        );

        assertTrue(AdminAuthorizationService.hasPermission(AdminPermission.AUCTION_MANAGE));
        assertFalse(AdminAuthorizationService.hasPermission(AdminPermission.USER_MANAGE));
        assertTrue(AdminAuthorizationService.hasRole(AdminRole.SUPPORT_ADMIN));
        assertFalse(AdminAuthorizationService.hasRole(AdminRole.FINANCE_ADMIN));
    }
}