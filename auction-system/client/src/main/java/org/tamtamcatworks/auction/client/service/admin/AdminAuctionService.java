package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.client.exception.AdminApiException;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

public class AdminAuctionService
        extends BaseAdminService {
    public void closeAuction(
        String auctionId
    ) {

        apiClient.client()
            .patch()
            .uri("/admin/auctions/{id}/close", auctionId)
            .retrieve()
            .toBodilessEntity();
    }

    public List<AuctionResponse> getAuctions() {

        return apiClient.client()
            .get()
            .uri("/admin/auctions")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }
}
