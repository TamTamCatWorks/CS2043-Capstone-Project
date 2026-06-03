package org.tamtamcatworks.auction.service.mapper;

import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.Electronics;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.Vehicle;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.response.ItemResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemMapperTest {

    private final ItemMapper itemMapper = loadMapper();

    // ── helpers ──────────────────────────────────────────────────────────────

    private static User seller() {
        return new User("seller", "seller@example.com", "hash", "Seller Name", 1000);
    }

    private static Art art(User seller) {
        return new Art(
            "Sunflowers",
            "A beautiful painting",
            5000,
            ItemCondition.NEW,
            "https://example.com/art.jpg",
            seller,
            "Van Gogh",
            1889,
            "Oil on canvas",
            "73x92 cm",
            true
        );
    }

    private static Vehicle vehicle(User seller) {
        return new Vehicle(
            "Tesla Model S",
            "Electric sedan",
            80000,
            ItemCondition.GOOD,
            "https://example.com/car.jpg",
            seller,
            "Tesla",
            "Model S",
            2022,
            15000,
            "Red",
            "Electric"
        );
    }

    private static Electronics electronics(User seller) {
        return new Electronics(
            "MacBook Pro",
            "Laptop",
            2000,
            ItemCondition.NEW,
            "https://example.com/laptop.jpg",
            seller,
            "Apple",
            "MacBook Pro M3",
            24
        );
    }

    // ── Art ──────────────────────────────────────────────────────────────────

    @Test
    void toResponseMapsArtFieldsCorrectly() {
        User seller = seller();
        Art art = art(seller);

        ItemResponse response = itemMapper.toResponse(art);

        assertEquals(art.getId(), response.id());
        assertEquals(art.getName(), response.name());
        assertEquals("ART", response.itemType());
        assertEquals(art.getStartingPrice(), response.startingPrice());
        assertEquals(ItemCondition.NEW.name(), response.condition());
        assertEquals(seller.getId(), response.sellerId());
        assertEquals(art.getDescription(), response.description());
        assertEquals(art.getImageUrl(), response.imageUrl());
    }

    @Test
    void toResponseArtWithNullSellerProducesNullSellerId() {
        Art art = art(null);

        ItemResponse response = itemMapper.toResponse(art);

        assertNull(response.sellerId());
    }

    // ── Vehicle ──────────────────────────────────────────────────────────────

    @Test
    void toResponseMapsVehicleFieldsCorrectly() {
        User seller = seller();
        Vehicle vehicle = vehicle(seller);

        ItemResponse response = itemMapper.toResponse(vehicle);

        assertEquals(vehicle.getId(), response.id());
        assertEquals(vehicle.getName(), response.name());
        assertEquals("VEHICLE", response.itemType());
        assertEquals(vehicle.getStartingPrice(), response.startingPrice());
        assertEquals(ItemCondition.GOOD.name(), response.condition());
        assertEquals(seller.getId(), response.sellerId());
        assertEquals(vehicle.getDescription(), response.description());
        assertEquals(vehicle.getImageUrl(), response.imageUrl());
    }

    @Test
    void toResponseVehicleWithNullSellerProducesNullSellerId() {
        Vehicle vehicle = vehicle(null);

        ItemResponse response = itemMapper.toResponse(vehicle);

        assertNull(response.sellerId());
    }

    // ── Electronics ──────────────────────────────────────────────────────────

    @Test
    void toResponseMapsElectronicsFieldsCorrectly() {
        User seller = seller();
        Electronics electronics = electronics(seller);

        ItemResponse response = itemMapper.toResponse(electronics);

        assertEquals(electronics.getId(), response.id());
        assertEquals(electronics.getName(), response.name());
        assertEquals("ELECTRONICS", response.itemType());
        assertEquals(electronics.getStartingPrice(), response.startingPrice());
        assertEquals(ItemCondition.NEW.name(), response.condition());
        assertEquals(seller.getId(), response.sellerId());
        assertEquals(electronics.getDescription(), response.description());
        assertEquals(electronics.getImageUrl(), response.imageUrl());
    }

    @Test
    void toResponseElectronicsWithNullSellerProducesNullSellerId() {
        Electronics electronics = electronics(null);

        ItemResponse response = itemMapper.toResponse(electronics);

        assertNull(response.sellerId());
    }

    // ── polymorphic toResponse(Item) ─────────────────────────────────────────

    @Test
    void toResponsePolymorphicDispatchesToArt() {
        Item item = art(seller());

        ItemResponse response = itemMapper.toResponse(item);

        assertEquals("ART", response.itemType());
    }

    @Test
    void toResponsePolymorphicDispatchesToVehicle() {
        Item item = vehicle(seller());

        ItemResponse response = itemMapper.toResponse(item);

        assertEquals("VEHICLE", response.itemType());
    }

    @Test
    void toResponsePolymorphicDispatchesToElectronics() {
        Item item = electronics(seller());

        ItemResponse response = itemMapper.toResponse(item);

        assertEquals("ELECTRONICS", response.itemType());
    }

    @Test
    void toResponsePolymorphicReturnsNullForNullItem() {
        ItemResponse response = itemMapper.toResponse((Item) null);

        assertNull(response);
    }

    // ── toResponses(List<Item>) ───────────────────────────────────────────────

    @Test
    void toResponsesMapsAllItemsInList() {
        User seller = seller();
        List<Item> items = List.of(art(seller), vehicle(seller), electronics(seller));

        List<ItemResponse> responses = itemMapper.toResponses(items);

        assertEquals(3, responses.size());
        assertEquals("ART", responses.get(0).itemType());
        assertEquals("VEHICLE", responses.get(1).itemType());
        assertEquals("ELECTRONICS", responses.get(2).itemType());
    }

    @Test
    void toResponsesReturnsEmptyListForNullInput() {
        List<ItemResponse> responses = itemMapper.toResponses(null);

        assertTrue(responses.isEmpty());
    }

    @Test
    void toResponsesReturnsEmptyListForEmptyInput() {
        List<ItemResponse> responses = itemMapper.toResponses(List.of());

        assertTrue(responses.isEmpty());
    }

    // ── loader ───────────────────────────────────────────────────────────────

    private static ItemMapper loadMapper() {
        try {
            Class<?> mapperImpl = Class.forName("org.tamtamcatworks.auction.service.mapper.ItemMapperImpl");
            return assertInstanceOf(ItemMapper.class, mapperImpl.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to load ItemMapperImpl", exception);
        }
    }
}
