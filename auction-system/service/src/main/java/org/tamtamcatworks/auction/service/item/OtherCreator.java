package org.tamtamcatworks.auction.service.item;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.tamtamcatworks.auction.model.item.Other;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;

@Component
public class OtherCreator extends ItemCreator {

    @Override
    protected Item buildItem(
        String name,
        String description,
        double startingPrice,
        ItemCondition condition,
        String imageUrl,
        User seller,
        Map<String, Object> details
    ) {
        return new Other(
            name,
            description,
            startingPrice,
            condition,
            imageUrl,
            seller
        );
    }

    @Override
    protected void validate(
        String name,
        String description,
        double startingPrice,
        ItemCondition condition,
        String imageUrl,
        User seller,
        Map<String, Object> details
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Starting price must be positive.");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition is required.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required.");
        }
        if (seller == null) {
            throw new IllegalArgumentException("Seller is required.");
        }
    }

    @Override
    public ItemType supports() {
        return ItemType.OTHER;
    }
}
