package org.tamtamcatworks.auction.model.item;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import org.tamtamcatworks.auction.model.user.User;

@Entity
@Table(name = "electronics_items")
@DiscriminatorValue("ELECTRONICS")
@PrimaryKeyJoinColumn(name = "item_id")
public class Electronics extends Item {

  @Column(nullable = false)
  private String brand;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false)
  private int warrantyMonths;

  public Electronics(
      String name,
      String description,
      double startingPrice,
      ItemCondition condition,
      String imageUrl,
      User seller,
      String brand,
      String model,
      int warrantyMonths) {

    super(name, description, startingPrice, condition, imageUrl, seller);

    setBrand(brand);
    setModel(model);
    setWarrantyMonths(warrantyMonths);
  }

  protected Electronics() {}

  @Override
  public String getSpecificInfo() {
    return "Hãng: " + brand + " | Model: " + model + " | Bảo hành: " + warrantyMonths + " tháng";
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {

    if (brand == null || brand.isBlank()) {
      throw new IllegalArgumentException("Brand is required.");
    }

    this.brand = brand.trim();
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {

    if (model == null || model.isBlank()) {
      throw new IllegalArgumentException("Model is required.");
    }

    this.model = model.trim();
  }

  public int getWarrantyMonths() {
    return warrantyMonths;
  }

  public void setWarrantyMonths(int warrantyMonths) {

    if (warrantyMonths < 0) {
      throw new IllegalArgumentException("Warranty months cannot be negative.");
    }

    this.warrantyMonths = warrantyMonths;
  }
}
