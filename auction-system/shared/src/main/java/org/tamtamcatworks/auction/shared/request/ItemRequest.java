package org.tamtamcatworks.auction.shared.request;

import java.util.Map;


public record ItemRequest(
    String itemType,
    String name,
    String description,
    double startingPrice,
    String condition,       // "NEW", "USED", "REFURBISHED" — just a string
    String imageUrl,
    Map<String, Object> details
) {}