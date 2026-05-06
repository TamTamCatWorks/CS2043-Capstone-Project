package org.tamtamcatworks.auction.model.item;

import org.tamtamcatworks.auction.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

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
            User seller,
            String brand,
            String model,
            int warrantyMonths) {
        super(name, description, startingPrice, condition, seller);
        this.brand = brand;
        this.model = model;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getSpecificInfo() {
        return "Hãng: " + brand
                + " | Model: " + model
                + " | Bảo hành: " + warrantyMonths + " tháng";
    }
}