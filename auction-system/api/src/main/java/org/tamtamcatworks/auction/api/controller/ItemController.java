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

import org.tamtamcatworks.auction.service.item.ItemService;

import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.response.ItemResponse;

import org.tamtamcatworks.auction.service.mapper.ItemMapper;

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

        String sellerId = (String) session.getAttribute("userId");

        ItemRequest requestWithSeller = new ItemRequest(
            req.itemType(),
            req.name(),
            req.description(),
            req.startingPrice(),
            req.condition(),
            sellerId,
            req.imageUrl(),
            req.details()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(itemService.createResponse(requestWithSeller));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> get(@PathVariable String id) {

        return ResponseEntity.ok(
            itemService.findResponseById(id)
        );
    }
}