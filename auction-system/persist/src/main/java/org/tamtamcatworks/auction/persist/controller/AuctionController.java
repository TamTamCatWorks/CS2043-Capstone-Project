package org.tamtamcatworks.auction.persist.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tamtamcatworks.auction.persist.dto.BidRequest;
import org.tamtamcatworks.auction.persist.dto.BidResponse;
import org.tamtamcatworks.auction.persist.service.BidService;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    private final BidService bidService;

    public AuctionController(BidService bidService) {
        this.bidService = bidService;
    }

    // bidderId comes from session, not request body
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
