package org.tamtamcatworks.auction.client.controller.details;

public final class ItemDetailsFormFactory {

  private ItemDetailsFormFactory() {}

  public static ItemDetailsForm create(ItemDetailsType type) {
    return switch (type) {
      case ART -> new ArtDetailsForm();
      case ELECTRONICS -> new ElectronicsDetailsForm();
      case VEHICLE -> new VehicleDetailsForm();
      case OTHER -> null;
    };
  }
}
