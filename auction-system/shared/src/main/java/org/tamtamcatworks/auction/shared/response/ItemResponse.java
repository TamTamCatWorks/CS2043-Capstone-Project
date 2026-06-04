package org.tamtamcatworks.auction.shared.response;

public record ItemResponse(
    String id,
    String name,
    String itemType,
    double startingPrice,
    String condition,
    String sellerId,
    String description,
    String imageUrl) {}
