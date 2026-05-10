package org.tamtamcatworks.auction.api.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.tamtamcatworks.auction.api.dto.ItemRequest;
import org.tamtamcatworks.auction.api.dto.ItemResponse;

import org.tamtamcatworks.auction.service.item.ItemService;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(

            @RequestBody ItemRequest req,

            HttpSession session

    ) {

        String sellerId =
                (String) session.getAttribute("userId");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ItemResponse.from(

                                itemService.create(

                                        req.itemType(),

                                        req.name(),

                                        req.description(),

                                        req.startingPrice(),

                                        req.condition(),

                                        req.imageUrl(),

                                        req.details(),

                                        sellerId
                                )
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> get(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                ItemResponse.from(
                        itemService.findById(id)
                )
        );
    }
}