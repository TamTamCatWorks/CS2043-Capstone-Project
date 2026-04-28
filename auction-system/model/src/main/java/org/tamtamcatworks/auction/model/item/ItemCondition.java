package org.tamtamcatworks.auction.model.item;


public enum ItemCondition {
    NEW("Mới"),

    LIKE_NEW("Như mới"),

    GOOD("Tốt"),

    FAIR("Khá"),

    POOR("Kém");


    private final String displayName;

    ItemCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    @Override
    public String toString() {
        return displayName;
    }
}