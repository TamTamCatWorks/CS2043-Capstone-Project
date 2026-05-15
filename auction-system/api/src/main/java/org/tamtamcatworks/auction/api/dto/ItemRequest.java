package org.tamtamcatworks.auction.api.dto;

import java.util.Map;

import org.tamtamcatworks.auction.model.item.ItemCondition;

public record ItemRequest(

        String itemType,

        String name,

        String description,

        double startingPrice,

        ItemCondition condition,

        String imageUrl,

        Map<String, Object> details

) {
}
