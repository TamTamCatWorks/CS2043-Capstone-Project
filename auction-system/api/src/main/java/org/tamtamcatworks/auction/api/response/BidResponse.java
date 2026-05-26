package org.tamtamcatworks.auction.api.response;

import org.tamtamcatworks.auction.model.BidTransaction;

import java.time.LocalDateTime;

public record BidResponse(String id, String auctionId, String bidderId, String bidderName,
                          double amount, BidTransaction.BidType bidType,
                          LocalDateTime createdAt) {

    public static BidResponse from(BidTransaction tx) {
        return new BidResponse(
            tx.getId(),
            tx.getAuction().getId(),
            tx.getBidder().getId(),
            tx.getBidder().getFullName(),
            tx.getAmount(),
            tx.getBidType(),
            tx.getCreationDate()
        );
    }
}
