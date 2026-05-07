package org.tamtamcatworks.auction.persist.dto;

import org.tamtamcatworks.auction.model.BidTransaction;

import java.time.LocalDateTime;

public record BidResponse(
    String id,
    String auctionId,
    String bidderId,
    String bidderName,
    double amount,
    String bidType,
    LocalDateTime createdAt
) {
    public static BidResponse from(BidTransaction tx) {
        return new BidResponse(
            tx.getId(),
            tx.getAuction().getId(),
            tx.getBidder().getId(),
            tx.getBidder().getUsername(),
            tx.getAmount(),
            tx.getBidType().name(),
            tx.getCreationDate()
        );
    }
}
