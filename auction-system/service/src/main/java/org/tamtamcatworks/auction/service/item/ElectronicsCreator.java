package org.tamtamcatworks.auction.service.item;
public class ElectronicsCreator extends ItemCreator {
    @Override
    protected Item buildItem(ItemRequest req, User seller) {
        Map<String, Object> d = req.details();
        return new Electronics(
            req.name(), req.description(), req.startingPrice(), req.condition(), seller,
            get(d, "brand"), get(d, "model"), getInt(d, "warrantyMonths")
        );
    }
}