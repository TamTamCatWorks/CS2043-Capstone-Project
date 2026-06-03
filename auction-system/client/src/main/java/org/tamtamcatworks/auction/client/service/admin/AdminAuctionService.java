package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

public class AdminAuctionService extends BaseAdminService {

        public List<AuctionResponse> getAuctions() {

                return apiClient.client()
                        .get()
                        .uri("/admin/auctions")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});
        }

        public AuctionResponse openAuction(String auctionId) {

                return apiClient.client()
                .patch()
                .uri("/admin/auctions/{id}/open", auctionId)
                .retrieve()
                .body(AuctionResponse.class);
        }

        public AuctionResponse closeAuction(String auctionId) {

                return apiClient.client()
                .patch()
                .uri("/admin/auctions/{id}/close", auctionId)
                .retrieve()
                .body(AuctionResponse.class);
        }

        public List<AuctionResponse> searchAuctions(String keyword) {

                return apiClient.searchAuctions(
                        keyword,
                        null,
                        null
                );
        }
}