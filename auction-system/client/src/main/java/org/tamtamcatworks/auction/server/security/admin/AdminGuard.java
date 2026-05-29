package org.tamtamcatworks.auction.server.security.admin;

import org.springframework.stereotype.Component;

import org.tamtamcatworks.auction.server.exception.ForbiddenException;
import org.tamtamcatworks.auction.shared.response.UserResponse;

@Component
public class AdminGuard {

    public void requireAdmin(
            UserResponse user
    ) {

        if (user == null || !user.isAdmin()) {

            throw new ForbiddenException(

                    "Admin access required"
            );
        }
    }
}