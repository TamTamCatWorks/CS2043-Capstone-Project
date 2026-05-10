package org.tamtamcatworks.auction.service.item;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;

@Component
public class ArtCreator extends ItemCreator {

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

        return new Art(
            name,
            description,
            startingPrice,
            condition,
            imageUrl,
            seller,
            get(details, "artist"),
            getInt(details, "yearCreated"),
            get(details, "medium"),
            get(details, "dimensions"),
            getBoolean(details, "hasCertificate")
        );
    }

    @Override
    public String supports() {
        return "ART";
    }
}