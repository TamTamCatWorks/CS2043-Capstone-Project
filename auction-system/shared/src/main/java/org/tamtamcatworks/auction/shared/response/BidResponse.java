package org.tamtamcatworks.auction.shared.response;
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
}