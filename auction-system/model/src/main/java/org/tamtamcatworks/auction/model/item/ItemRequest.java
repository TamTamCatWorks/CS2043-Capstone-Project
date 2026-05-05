package org.tamtamcatworks.auction.model.item;

import java.util.Map;

public class ItemRequest {
    public String id;
    public String name;
    public double startingPrice;
    public String description;
    public ItemCondition condition;
    public String sellerId;
    public String model;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public String getSellerId() {
        return sellerId;
    }

    //electronics
    public String brand;
    public int warrantyMonths;

    //art
    public String artist;
    public int yearCreated;
    public String medium;
    public boolean hasCertificate;

    //vehicle
    public String make;
    public int year;
    public int mileageKm;
    public String color;
    public String fuelType;

    public Map<String, Object> extra;

    public String get(String key) {
        return (String) extra.get(key);
    }

    public int getInt(String key) {
        return (int) extra.get(key);
    }

    public boolean getBoolean(String key) {
        return (boolean) extra.get(key);
    }
}
