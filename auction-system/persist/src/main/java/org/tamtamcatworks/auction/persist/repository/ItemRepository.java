package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.ItemType;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByType(ItemType type);
    List<Item> findByCondition(ItemCondition condition);
    List<Item> findByStartingPriceBetween(Double min, Double max);
}
