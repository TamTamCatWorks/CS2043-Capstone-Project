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
        String imageUrl,
        User seller,
        String make,
        String model,
        int year,
        int mileageKm,
        String color,
        String fuelType
) {

    super(
        name,
        description,
        startingPrice,
        condition,
        imageUrl,
        seller
    );

    setMake(make);
    setModel(model);
    setYear(year);
    setMileageKm(mileageKm);
    setColor(color);
    setFuelType(fuelType);
}

    protected Vehicle() {}

    @Override
    public String getSpecificInfo() {
        return make + " " + model + " " + year
                + " | " + mileageKm + " km"
                + " | Màu: " + color
                + " | Nhiên liệu: " + fuelType;
    }

    public String getMake() {
    return make;
}

public void setMake(String make) {

    if (make == null || make.isBlank()) {
        throw new IllegalArgumentException("Make is required.");
    }

        this.make = make.trim();
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {

        if (year <= 0) {
            throw new IllegalArgumentException("Invalid year.");
        }

        this.year = year;
    }

    public int getMileageKm() {
        return mileageKm;
    }

    public void setMileageKm(int mileageKm) {

        if (mileageKm < 0) {
            throw new IllegalArgumentException("Mileage cannot be negative.");
        }

        this.mileageKm = mileageKm;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {

        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("Color is required.");
        }

        this.color = color.trim();
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {

        if (fuelType == null || fuelType.isBlank()) {
            throw new IllegalArgumentException("Fuel type is required.");
        }

        this.fuelType = fuelType.trim();
    }
}