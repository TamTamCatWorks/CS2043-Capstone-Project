package org.tamtamcatworks.auction.api.dto;

import org.tamtamcatworks.auction.model.item.Item;

public record ItemResponse(String id, String name, double startingPrice) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
            item.getId().toString(),
            item.getName(),
            item.getStartingPrice()
        );
    }
}