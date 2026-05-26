package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, String> {
    @Query("SELECT i FROM Item i WHERE TYPE(i) = :clazz")
    List<Item> findByItemClass(Class<? extends Item> clazz);
    List<Item> findByCondition(ItemCondition condition);
    List<Item> findByStartingPriceBetween(Double min, Double max);
}
