package org.tamtamcatworks.auction.service.item;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.tamtamcatworks.auction.model.item.Electronics;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;

@Component
public class ElectronicsCreator extends ItemCreator {

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

        return new Electronics(

                name,

                description,

                startingPrice,

                condition,

                imageUrl,

                seller,

                get(details, "brand"),

                get(details, "model"),

                getInt(details, "warrantyMonths")
        );
    }

    @Override
    public String supports() {
        return "ELECTRONICS";
    }
}