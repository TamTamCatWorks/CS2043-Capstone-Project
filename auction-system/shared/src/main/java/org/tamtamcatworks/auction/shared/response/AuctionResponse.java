package org.tamtamcatworks.auction.shared.response;


import java.time.LocalDateTime;

public record AuctionResponse(
    String title,
    String sellerId,        // was User seller
    String sellerName,      // just what the client needs
    String itemId,          // was Item item
    String itemName,        // just what the client needs
    double startingPrice,
    LocalDateTime startTime,
    LocalDateTime endTime
) {}