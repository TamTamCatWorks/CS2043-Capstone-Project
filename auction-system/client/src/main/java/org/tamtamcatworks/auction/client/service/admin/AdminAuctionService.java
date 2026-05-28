package org.tamtamcatworks.auction.client.service.admin;

import org.tamtamcatworks.auction.client.exception.AdminApiException;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

public class AdminAuctionService
        extends BaseAdminService {
    public void closeAuction(
        String auctionId
    ) {

        try {

            apiClient.closeAuction(
                    auctionId
            );

        } catch (Exception ex) {

            throw new AdminApiException(
                    "Failed to close auction",
                    ex
            );
        }
    }

    public java.util.List<AuctionResponse>
        getAuctions() {

        try {

            return apiClient.getAllAuctions();

        } catch (Exception ex) {

            throw new AdminApiException(
                    "Failed to load auctions",
                    ex
            );
        }
    }
}
