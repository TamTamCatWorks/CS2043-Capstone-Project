package org.tamtamcatworks.auction.shared.response;

import java.time.LocalDateTime;

public record AuctionResponse(
    String title,
    String sellerId,
    String sellerName,
    String itemId,
    String itemName,
    double startingPrice,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
