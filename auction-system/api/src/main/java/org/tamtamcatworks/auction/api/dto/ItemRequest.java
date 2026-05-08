package org.tamtamcatworks.auction.api.dto;

import java.util.Map;

import org.tamtamcatworks.auction.model.item.ItemCondition;

public record ItemRequest(
    String name,
    String description,
    double startingPrice,
    ItemCondition condition,
    String sellerId,
    String itemType,             // "ART", "ELECTRONICS", "VEHICLE"
    Map<String, Object> details  // type-specific fields
) {}

