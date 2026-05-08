package org.tamtamcatworks.auction.service.item;

import java.util.Map;


import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.ItemRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

public abstract class ItemCreator {

    public Item create(String name, String description,
                   double startingPrice, ItemCondition condition,
                   User seller, Map<String, Object> details) {
        validate(name, description, startingPrice, condition, seller, details);
        return buildItem(name, description, startingPrice, condition, seller, details);
    };
    
    protected abstract Item buildItem(String name, String description,
                                   double startingPrice, ItemCondition condition,
                                   User seller, Map<String, Object> details);

    protected abstract Item buildItem(ItemRequest req, User seller);


    protected void validate(ItemRequest req) {
        if (req.name() == null || req.name().isBlank())
            throw new IllegalArgumentException("Item name is required.");
        if (req.details() == null)
            throw new IllegalArgumentException("Item details are required.");
    }

    protected String get(Map<String, Object> details, String key) {
        Object val = details.get(key);
        if (val == null) throw new IllegalArgumentException("Missing field: " + key);
        return val.toString();
    }

    protected int getInt(Map<String, Object> details, String key) {
        Object val = details.get(key);
        if (val == null) throw new IllegalArgumentException("Missing field: " + key);
        return ((Number) val).intValue();
    }

    protected boolean getBoolean(Map<String, Object> details, String key) {
        Object val = details.get(key);
        if (val == null) throw new IllegalArgumentException("Missing field: " + key);
        return (Boolean) val;
    }
}
