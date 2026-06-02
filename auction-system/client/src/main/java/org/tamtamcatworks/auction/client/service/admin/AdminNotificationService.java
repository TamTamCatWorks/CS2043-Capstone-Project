package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

public class AdminNotificationService
        extends BaseAdminService {

    public List<NotificationResponse> getNotifications() {

        return apiClient.client()
                .get()
                .uri("/admin/notifications")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}