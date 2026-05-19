package org.tamtamcatworks.auction.api.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tamtamcatworks.auction.api.request.BidRequest;
import org.tamtamcatworks.auction.api.response.BidResponse;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.service.auction.AuctionService;
import org.tamtamcatworks.auction.service.auction.BidService;
import org.tamtamcatworks.auction.shared.request.AuctionRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final BidService bidService;

    public AuctionController(AuctionService auctionService, BidService bidService) {
        this.auctionService = auctionService;
        this.bidService = bidService;
    }

    @PostMapping("/existing-item")
    public ResponseEntity<AuctionResponse> createWithExistingItem(
        @RequestBody AuctionRequest req,
        HttpSession session
    ) {
        String sellerId = (String) session.getAttribute("userId");
        Auction auction = auctionService.create(
            sellerId,
            req.itemId(),
            req.title(),
            req.startingPrice(),
            req.startTime(),
            req.endTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.toResponse(auction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(auctionService.toResponse(auctionService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getByStatus(@RequestParam AuctionStatus status) {
        List<Auction> auctions = auctionService.findByStatus(status);
        return ResponseEntity.ok(auctionService.toResponses(auctions));
    }

    @PatchMapping("/{id}/open")
    public ResponseEntity<AuctionResponse> open(@PathVariable String id) {
        return ResponseEntity.ok(auctionService.toResponse(auctionService.open(id)));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AuctionResponse> close(@PathVariable String id) {
        return ResponseEntity.ok(auctionService.toResponse(auctionService.close(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AuctionResponse> cancel(@PathVariable String id,
                                                  @RequestParam String reason) {
        return ResponseEntity.ok(auctionService.toResponse(auctionService.cancel(id, reason)));
    }

    @PostMapping("/{id}/bids")
    public ResponseEntity<BidResponse> placeBid(@PathVariable String id,
                                                @RequestBody BidRequest req,
                                                HttpSession session) {
        String bidderId = (String) session.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED).body(BidResponse.from(
            bidService.placeBid(id, bidderId, req.amount(), req.bidType())
        ));
    }

    @GetMapping("/{id}/bids")
    public ResponseEntity<List<BidResponse>> getBids(@PathVariable String id) {
        return ResponseEntity.ok(bidService.findByAuction(id)
            .stream().map(BidResponse::from).toList());
    }
}
