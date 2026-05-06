package org.tamtamcatworks.auction.model.item;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import org.tamtamcatworks.auction.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;

@Entity
@Table(name = "vehicle_items")
@DiscriminatorValue("VEHICLE")
@PrimaryKeyJoinColumn(name = "item_id")
public class Vehicle extends Item {

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int mileageKm;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String fuelType;

    public Vehicle(
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            User seller,
            String make,
            String model,
            int year,
            int mileageKm,
            String color,
            String fuelType) {
        super(name, description, startingPrice, condition, seller);
        if (mileageKm < 0) {
            throw new IllegalArgumentException("Số km đã đi không được âm.");
        }
        this.make = make;
        this.model = model;
        this.year = year;
        this.mileageKm = mileageKm;
        this.color = color;
        this.fuelType = fuelType;
    }

    @Override
    public String getSpecificInfo() {
        return make + " " + model + " " + year
                + " | " + mileageKm + " km"
                + " | Màu: " + color
                + " | Nhiên liệu: " + fuelType;
    }
}