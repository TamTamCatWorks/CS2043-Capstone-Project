package org.tamtamcatworks.auction.model.item;

import org.tamtamcatworks.auction.model.user.User;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "other_items")
@DiscriminatorValue("OTHER")
@PrimaryKeyJoinColumn(name = "item_id")
public class Other extends Item {

    public Other(
        String name,
        String description,
        double startingPrice,
        ItemCondition condition,
        String imageUrl,
        User seller
    ) {
        super(
            name,
            description,
            startingPrice,
            condition,
            imageUrl,
            seller
        );
    }
    
    protected Other() {}

    @Override
    public String getSpecificInfo() {
        return "";
    }
}
