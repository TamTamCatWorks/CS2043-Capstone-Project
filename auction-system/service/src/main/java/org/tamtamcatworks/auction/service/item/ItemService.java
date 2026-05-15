package org.tamtamcatworks.auction.service.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;

import org.tamtamcatworks.auction.persist.repository.ItemRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final List<ItemCreator> creators;
    private Map<ItemType, ItemCreator> registry;

    public ItemService(
        ItemRepository itemRepository,
        UserRepository userRepository,
        List<ItemCreator> creators
    ) {

        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.creators = creators;
    }

    @PostConstruct
    void init() {

        registry = new HashMap<>();

        for (ItemCreator creator : creators) {

            registry.put(
                creator.supports(),
                creator
            );
        }
    }

    @Transactional
    public Item create(
            String itemType,
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String imageUrl,
            Map<String, Object> details,
            String sellerId
    ) {
            
        if (sellerId == null || sellerId.isBlank()) {

            throw new IllegalArgumentException("Seller ID is required.");
        }

        if (itemType == null || itemType.isBlank()) {
            throw new IllegalArgumentException("Item type is required.");
        }

        User seller = userRepository
            .findById(sellerId)
            .orElseThrow(() ->
                    new NoSuchElementException("Seller not found.")
            );

        ItemType type = ItemType.fromKey(itemType);

        ItemCreator creator = registry.get(type);

        if (creator == null) {
            throw new IllegalArgumentException("Unknown item type: " + itemType);
        }

        Item item = creator.create(
            name,
            description,
            startingPrice,
            condition,
            imageUrl,
            seller,
            details
        );

        return itemRepository.save(
            java.util.Objects.requireNonNull(
                item,
                "Item must not be null"
            )
        );
    }

    @Transactional(readOnly = true)
    public Item findById(String id) {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException("Item ID is required.");
        }

        return itemRepository
            .findById(id)
            .orElseThrow(() ->
                    new NoSuchElementException("Item not found.")
            );
    }
}