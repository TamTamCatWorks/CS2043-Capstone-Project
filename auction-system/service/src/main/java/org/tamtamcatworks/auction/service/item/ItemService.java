package org.tamtamcatworks.auction.service.item;

import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.ItemRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final Map<String, ItemCreator> registry;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.registry = Map.of(
            "ART", new ArtCreator(),
            "ELECTRONICS", new ElectronicsCreator(),
            "VEHICLE", new VehicleCreator()
        );
    }

    @Transactional
    public Item create(String sellerId, String itemType,
                    String name, String description,
                    double startingPrice, ItemCondition condition,
                    Map<String, Object> details) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new NoSuchElementException("Seller not found."));
        ItemCreator creator = registry.get(itemType);
        if (creator == null)
            throw new IllegalArgumentException("Unknown item type: " + itemType);
        return itemRepository.save(creator.create(name, description,
            startingPrice, condition, seller, details));
    }

    @Transactional(readOnly = true)
    public Item findById(String id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Item not found."));
    }
}