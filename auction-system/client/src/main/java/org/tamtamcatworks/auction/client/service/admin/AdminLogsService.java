package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

public class AdminLogsService extends BaseAdminService {

    public List<String> getLogs() {

        return apiClient.client()
                .get()
                .uri("/admin/logs")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}