package org.tamtamcatworks.auction.api.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.service.auction.AuctionService;
import org.tamtamcatworks.auction.api.dto.AuctionResponse;
import org.tamtamcatworks.auction.api.dto.AuctionRequest;
import org.tamtamcatworks.auction.service.auction.CreateAuctionRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    // seller flow — one form creates both item and auction
    @PostMapping
    public ResponseEntity<AuctionResponse> create(@RequestBody CreateAuctionRequest req,
                                                  HttpSession session) {
        String sellerId = (String) session.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED).body(AuctionResponse.from(
                auctionService.createWithItem(sellerId, req)
        ));
    }

    // advanced flow — auction for an already existing item
    @PostMapping("/existing-item")
    public ResponseEntity<AuctionResponse> createWithExistingItem(
            @RequestBody AuctionRequest req, HttpSession session) {
        String sellerId = (String) session.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED).body(AuctionResponse.from(
                auctionService.create(sellerId, req.itemId(), req.title(),
                        req.startingPrice(), req.startTime(), req.endTime())
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(AuctionResponse.from(auctionService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getByStatus(@RequestParam AuctionStatus status) {
        List<Auction> auctions = auctionService.findByStatus(status);
        List<AuctionResponse> responses = auctions.stream()
                .map(AuctionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/open")
    public ResponseEntity<AuctionResponse> open(@PathVariable String id) {
        return ResponseEntity.ok(AuctionResponse.from(auctionService.open(id)));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AuctionResponse> close(@PathVariable String id) {
        return ResponseEntity.ok(AuctionResponse.from(auctionService.close(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AuctionResponse> cancel(@PathVariable String id,
                                                  @RequestParam String reason) {
        return ResponseEntity.ok(AuctionResponse.from(auctionService.cancel(id,reason)));
    }
}