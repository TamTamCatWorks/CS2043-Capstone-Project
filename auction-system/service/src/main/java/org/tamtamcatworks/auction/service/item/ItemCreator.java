package org.tamtamcatworks.auction.service.item;

import java.util.Map;
import java.util.Objects;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;

public abstract class ItemCreator {

  public Item create(
      String name,
      String description,
      double startingPrice,
      ItemCondition condition,
      String imageUrl,
      User seller,
      Map<String, Object> details) {

    validate(name, description, startingPrice, condition, imageUrl, seller, details);

    return Objects.requireNonNull(
        buildItem(name, description, startingPrice, condition, imageUrl, seller, details),
        "buildItem() must not return null");
  }

  protected abstract Item buildItem(
      String name,
      String description,
      double startingPrice,
      ItemCondition condition,
      String imageUrl,
      User seller,
      Map<String, Object> details);

  protected void validate(
      String name,
      String description,
      double startingPrice,
      ItemCondition condition,
      String imageUrl,
      User seller,
      Map<String, Object> details) {

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

    if (details == null) {

      throw new IllegalArgumentException("Item details are required.");
    }
  }

  protected String get(Map<String, Object> details, String key) {

    Object val = details.get(key);

    if (val == null) {

      throw new IllegalArgumentException("Missing field: " + key);
    }

    return val.toString();
  }

  protected int getInt(Map<String, Object> details, String key) {

    Object val = details.get(key);

    if (val == null) {

      throw new IllegalArgumentException("Missing field: " + key);
    }

    if (!(val instanceof Number number)) {

      throw new IllegalArgumentException(key + " must be numeric.");
    }

    return number.intValue();
  }

  protected boolean getBoolean(Map<String, Object> details, String key) {

    Object val = details.get(key);

    if (val == null) {

      throw new IllegalArgumentException("Missing field: " + key);
    }

    if (!(val instanceof Boolean bool)) {

      throw new IllegalArgumentException(key + " must be boolean.");
    }

    return bool;
  }

  public abstract ItemType supports();
}
