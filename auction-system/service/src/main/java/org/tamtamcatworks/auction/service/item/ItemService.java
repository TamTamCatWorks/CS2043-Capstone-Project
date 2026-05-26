package org.tamtamcatworks.auction.service.item;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.request.ItemRequest;

import org.tamtamcatworks.auction.persist.repository.ItemRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

import org.tamtamcatworks.auction.service.mapper.ItemMapper;
import org.tamtamcatworks.auction.shared.response.ItemResponse;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final List<ItemCreator> creators;
    private final ItemMapper itemMapper;

    private Map<ItemType, ItemCreator> registry;

    public ItemService(
        ItemRepository itemRepository,
        UserRepository userRepository,
        List<ItemCreator> creators,
        ItemMapper itemMapper
    ) {

        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.creators = creators;
        this.itemMapper = itemMapper;
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
            String condition,
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
            parseCondition(condition),
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
    public ItemResponse findResponseById(String id) {

        Item item = findById(id);

        return itemMapper.toResponse(item);
    }

    public List<ItemResponse> findAllResponses() {

        List<Item> items = itemRepository.findAll();

        return itemMapper.toResponses(items);
    }

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

    public List<Item> findAll() {

        return itemRepository.findAll();
    }

    @Transactional
    public Item create(ItemRequest req) {

        if (req.sellerId() == null || req.sellerId().isBlank()) {
            throw new IllegalArgumentException("Seller ID is required.");
        }

        return create(
            req.itemType(),
            req.name(),
            req.description(),
            req.startingPrice(),
            req.condition(),
            req.imageUrl(),
            req.details(),
            req.sellerId()
        );
    }

    private ItemCondition parseCondition(String condition) {
        if (condition == null || condition.isBlank()) {
            throw new IllegalArgumentException("Condition is required.");
        }
        try {
            return ItemCondition.valueOf(condition.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown item condition: " + condition, exception);
        }
    }

    @Transactional
    public ItemResponse createResponse(ItemRequest req) {

        Item item = create(req);

        return itemMapper.toResponse(item);
    }
}
