package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.client.exception.AdminApiException;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class AdminUserService
        extends BaseAdminService {

    public List<UserResponse> getUsers() {

        return apiClient.client()
            .get()
            .uri("/admin/users")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    public void suspendUser(
        String userId
    ) {

        apiClient.client()
            .patch()
            .uri("/admin/users/{id}/suspend", userId)
            .retrieve()
            .toBodilessEntity();
    }

    public void activateUser(
        String userId
    ) {

        apiClient.client()
            .patch()
            .uri("/admin/users/{id}/activate", userId)
            .retrieve()
            .toBodilessEntity();
    }
}