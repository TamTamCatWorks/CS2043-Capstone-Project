package org.tamtamcatworks.auction.api.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.service.auction.AuctionService;
import org.tamtamcatworks.auction.service.auction.AutoBidService;
import org.tamtamcatworks.auction.service.auction.BidService;
import org.tamtamcatworks.auction.shared.request.AuctionRequest;
import org.tamtamcatworks.auction.shared.request.AutoBidRequest;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.AutoBidResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import org.tamtamcatworks.auction.shared.response.PageResponse;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

  private final AuctionService auctionService;
  private final BidService bidService;
  private final AutoBidService autoBidService;

  public AuctionController(
      AuctionService auctionService, BidService bidService, AutoBidService autoBidService) {
    this.auctionService = auctionService;
    this.bidService = bidService;
    this.autoBidService = autoBidService;
  }

  @PostMapping
  public ResponseEntity<AuctionResponse> create(
      @RequestBody CreateAuctionRequest req, HttpSession session) {
    String sellerId = requireUserId(session);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(auctionService.createByRequest(sellerId, req));
  }

  @PostMapping("/existing-item")
  public ResponseEntity<AuctionResponse> createWithExistingItem(
      @RequestBody AuctionRequest req, HttpSession session) {
    String sellerId = requireUserId(session);
    return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.create(sellerId, req));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AuctionResponse> get(@PathVariable String id) {
    return ResponseEntity.ok(auctionService.findResponseById(id));
  }

  @GetMapping
  public ResponseEntity<List<AuctionResponse>> getByStatus(@RequestParam AuctionStatus status) {
    return ResponseEntity.ok(auctionService.findResponsesByStatus(status));
  }

  @GetMapping("/search")
  public ResponseEntity<PageResponse<AuctionResponse>> search(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) AuctionStatus status,
      @RequestParam(required = false) String category,
      Pageable pageable) {
    PageResponse<AuctionResponse> page =
        auctionService.searchResponsesPage(q, status, category, pageable);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/search/list")
  public ResponseEntity<List<AuctionResponse>> searchAsList(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) AuctionStatus status,
      @RequestParam(required = false) String category) {
    return ResponseEntity.ok(auctionService.searchResponses(q, status, category));
  }

  @PatchMapping("/{id}/open")
  public ResponseEntity<AuctionResponse> open(@PathVariable String id) {
    return ResponseEntity.ok(auctionService.openById(id));
  }

  @PatchMapping("/{id}/close")
  public ResponseEntity<AuctionResponse> close(@PathVariable String id) {
    return ResponseEntity.ok(auctionService.closeById(id));
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<AuctionResponse> cancel(
      @PathVariable String id, @RequestParam String reason) {
    return ResponseEntity.ok(auctionService.cancelById(id, reason));
  }

  @PostMapping("/{id}/bids")
  public ResponseEntity<BidResponse> placeBid(
      @PathVariable String id, @RequestBody BidRequest req, HttpSession session) {
    String bidderId = requireUserId(session);
    return ResponseEntity.status(HttpStatus.CREATED).body(bidService.placeBid(id, bidderId, req));
  }

  @GetMapping("/{id}/bids")
  public ResponseEntity<List<BidResponse>> getBids(@PathVariable String id) {
    return ResponseEntity.ok(bidService.findByAuction(id));
  }

  @PostMapping("/{id}/auto-bid")
  public ResponseEntity<AutoBidResponse> registerAutoBid(
      @PathVariable String id, @RequestBody AutoBidRequest req, HttpSession session) {
    String bidderId = requireUserId(session);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(autoBidService.register(id, bidderId, req));
  }

  @GetMapping("/{id}/auto-bid")
  public ResponseEntity<AutoBidResponse> getAutoBid(@PathVariable String id, HttpSession session) {
    String bidderId = requireUserId(session);
    return ResponseEntity.ok(autoBidService.getByAuctionAndBidder(id, bidderId));
  }

  @DeleteMapping("/{id}/auto-bid")
  public ResponseEntity<Void> cancelAutoBid(@PathVariable String id, HttpSession session) {
    String bidderId = requireUserId(session);
    autoBidService.cancel(id, bidderId);
    return ResponseEntity.noContent().build();
  }

  private String requireUserId(HttpSession session) {

    return Objects.requireNonNull(
        (String) session.getAttribute("userId"), "User is not authenticated.");
  }
}
