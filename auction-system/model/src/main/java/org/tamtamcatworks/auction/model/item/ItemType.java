package org.tamtamcatworks.auction.model.item;

public enum ItemType {
    ART("Nghệ thuật"),
    VEHICLE("Phương tiện"),
    ELECTRONICS("Điện tử");

    private final String DisplayName;

    ItemType(String displayName) {
        this.DisplayName = displayName;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    @Override
    public String toString() {
        return DisplayName;
    }
}