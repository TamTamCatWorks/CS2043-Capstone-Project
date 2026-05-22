package org.tamtamcatworks.auction.service.item;

import java.util.Locale;

public enum ItemType {

    ART,
    ELECTRONICS,
    VEHICLE,
    OTHER;

    public static ItemType fromKey(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Item type is required.");
        }

        try {
            return ItemType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown item type: " + value, exception);
        }
    }
}
