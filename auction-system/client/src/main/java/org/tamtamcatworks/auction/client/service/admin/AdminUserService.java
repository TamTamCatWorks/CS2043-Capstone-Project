package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;

import org.tamtamcatworks.auction.client.exception.AdminApiException;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class AdminUserService
        extends BaseAdminService {

    public List<UserResponse> getUsers() {

        try {

            return List.of(
                    apiClient.getUser("1")
            );

        } catch (Exception ex) {

            throw new AdminApiException(
                    "Failed to load users",
                    ex
            );
        }
    }

    public void suspendUser(String userId) {

        try {

        /*
         * TODO:
         * backend endpoint later
         */

            System.out.println("Suspended user: " + userId);

        } catch (Exception ex) {

            throw new AdminApiException(
                "Failed to suspend user",
                ex
            );
        }
    }

    public void activateUser(String userId) {

        try {

            System.out.println("Activated user: " + userId);

        } catch (Exception ex) {

            throw new AdminApiException(
                "Failed to activate user",
                ex
            );
        }
    }
}