package org.tamtamcatworks.auction.shared.response;

import java.time.LocalDateTime;

public record AuctionResponse(
    String id,
    String title,
    String sellerId,
    String sellerName,
    String itemId,
    String itemName,
    String leadingBidderId,
    String leadingBidderName,
    double startingPrice,
    double currentPrice,
    double minimumIncrement,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String imageUrl,
    String itemDescription,
    String itemType,
    String specificInfo
) {
}
