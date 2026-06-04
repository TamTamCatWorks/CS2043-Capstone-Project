package org.tamtamcatworks.auction.client.controller.details;

public enum ItemDetailsType {
  ART("Art", true),
  ELECTRONICS("Electronics", true),
  VEHICLE("Vehicle", true),
  OTHER("Other", false);

  private final String displayName;
  private final boolean requiresDetails;

  ItemDetailsType(String displayName, boolean requiresDetails) {
    this.displayName = displayName;
    this.requiresDetails = requiresDetails;
  }

  public String displayName() {
    return displayName;
  }

  public boolean requiresDetails() {
    return requiresDetails;
  }

  public static ItemDetailsType fromDisplayName(String displayName) {
    if (displayName == null) {
      return null;
    }

    for (ItemDetailsType type : values()) {
      if (type.displayName.equalsIgnoreCase(displayName)) {
        return type;
      }
    }
    return null;
  }
}
