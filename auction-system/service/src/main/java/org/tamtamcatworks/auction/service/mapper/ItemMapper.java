package org.tamtamcatworks.auction.service.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.Electronics;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.Vehicle;

import org.tamtamcatworks.auction.shared.response.ItemResponse;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "itemType", constant = "ART")
    @Mapping(
        target = "condition",
        expression = "java(art.getCondition().name())"
    )
    @Mapping(
        target = "sellerId",
        expression =
            "java(art.getSeller() != null ? art.getSeller().getId() : null)"
    )
    ItemResponse toResponse(Art art);

    @Mapping(target = "itemType", constant = "VEHICLE")
    @Mapping(
        target = "condition",
        expression = "java(vehicle.getCondition().name())"
    )
    @Mapping(
        target = "sellerId",
        expression =
            "java(vehicle.getSeller() != null ? vehicle.getSeller().getId() : null)"
    )
    ItemResponse toResponse(Vehicle vehicle);

    @Mapping(target = "itemType", constant = "ELECTRONICS")
    @Mapping(
        target = "condition",
        expression = "java(electronics.getCondition().name())"
    )
    @Mapping(
        target = "sellerId",
        expression =
            "java(electronics.getSeller() != null ? electronics.getSeller().getId() : null)"
    )
    ItemResponse toResponse(Electronics electronics);

    default ItemResponse toResponse(Item item) {

        if (item == null) {
            return null;
        }

        if (item instanceof Art art) {
            return toResponse(art);
        }

        if (item instanceof Vehicle vehicle) {
            return toResponse(vehicle);
        }

        if (item instanceof Electronics electronics) {
            return toResponse(electronics);
        }

        throw new IllegalArgumentException(
            "Unsupported item type: " + item.getClass().getName()
        );
    }

    default List<ItemResponse> toResponses(List<Item> items) {

        if (items == null) {
            return List.of();
        }

        return items.stream()
            .map(this::toResponse)
            .toList();
    }
}