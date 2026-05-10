package org.tamtamcatworks.auction.service.item;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.Vehicle;
import org.tamtamcatworks.auction.model.user.User;

@Component
public class VehicleCreator extends ItemCreator {

    @Override
    protected Item buildItem(

            String name,

            String description,

            double startingPrice,

            ItemCondition condition,

            String imageUrl,

            User seller,

            Map<String, Object> details

    ) {

        return new Vehicle(
            name,
            description,
            startingPrice,
            condition,
            imageUrl,
            seller,
            get(details, "make"),
            get(details, "model"),
            getInt(details, "year"),
            getInt(details, "mileageKm"),
            get(details, "color"),
            get(details, "fuelType")
        );
    }

    @Override
    public String supports() {
        return "VEHICLE";
    }
}