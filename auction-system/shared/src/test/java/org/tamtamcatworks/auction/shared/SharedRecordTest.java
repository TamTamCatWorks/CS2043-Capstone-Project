package org.tamtamcatworks.auction.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.shared.request.AuctionRequest;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.request.LoginRequest;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.request.TopUpRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import org.tamtamcatworks.auction.shared.response.ItemResponse;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;
import org.tamtamcatworks.auction.shared.response.PageResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;
import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;
import org.tamtamcatworks.auction.shared.response.AdminDashboardResponse;
import org.tamtamcatworks.auction.shared.response.AdminReportResponse;

/**
 * Unit tests for standard request and response records in the shared module.
 */
public class SharedRecordTest {

  @Test
  public void testRequests() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime end = now.plusDays(1);

    // LoginRequest
    LoginRequest login = new LoginRequest("user@example.com", "pass123");
    assertEquals("user@example.com", login.email());
    assertEquals("pass123", login.password());

    // RegisterRequest
    RegisterRequest reg = new RegisterRequest("user", "user@example.com", "pass", "Full Name");
    assertEquals("user", reg.username());
    assertEquals("user@example.com", reg.email());
    assertEquals("pass", reg.password());
    assertEquals("Full Name", reg.fullName());

    // TopUpRequest
    TopUpRequest topUp = new TopUpRequest(100.0);
    assertEquals(100.0, topUp.amount());

    // BidRequest
    BidRequest bid = new BidRequest(500.0, "MANUAL");
    assertEquals(500.0, bid.amount());
    assertEquals("MANUAL", bid.bidType());

    // ItemRequest
    Map<String, Object> details = new HashMap<>();
    details.put("color", "red");
    ItemRequest itemReq = new ItemRequest(
        "ART", "Painting", "A nice painting", 10.0, "NEW", "seller-id", "img-url", details
    );
    assertEquals("ART", itemReq.itemType());
    assertEquals("Painting", itemReq.name());
    assertEquals("A nice painting", itemReq.description());
    assertEquals(10.0, itemReq.startingPrice());
    assertEquals("NEW", itemReq.condition());
    assertEquals("seller-id", itemReq.sellerId());
    assertEquals("img-url", itemReq.imageUrl());
    assertEquals(details, itemReq.details());

    // CreateAuctionRequest
    CreateAuctionRequest createAuc = new CreateAuctionRequest("Title", itemReq, now, end);
    assertEquals("Title", createAuc.title());
    assertEquals(itemReq, createAuc.item());
    assertEquals(now, createAuc.startTime());
    assertEquals(end, createAuc.endTime());

    // AuctionRequest
    AuctionRequest aucReq = new AuctionRequest("Title", "item-123", 20.0, now, end);
    assertEquals("Title", aucReq.title());
    assertEquals("item-123", aucReq.itemId());
    assertEquals(20.0, aucReq.startingPrice());
    assertEquals(now, aucReq.startTime());
    assertEquals(end, aucReq.endTime());
  }

  @Test
  public void testResponses() {
    LocalDateTime now = LocalDateTime.now();

    // AdminAuditLogResponse
    AdminAuditLogResponse audit = new AdminAuditLogResponse(
        "id-1", "adminName", "DELETE", "target", now
    );
    assertEquals("id-1", audit.id());
    assertEquals("adminName", audit.adminName());
    assertEquals("DELETE", audit.action());
    assertEquals("target", audit.target());
    assertEquals(now, audit.timestamp());

    // AdminDashboardResponse
    AdminDashboardResponse dash = new AdminDashboardResponse(10, 5, 2, 1500.0);
    assertEquals(10, dash.totalUsers());
    assertEquals(5, dash.activeAuctions());
    assertEquals(2, dash.pendingReports());
    assertEquals(1500.0, dash.totalRevenue());

    // AdminReportResponse
    AdminReportResponse report = new AdminReportResponse(
        "r-1", "USER", "spam-user", "spamming", "PENDING"
    );
    assertEquals("r-1", report.id());
    assertEquals("USER", report.targetType());
    assertEquals("spam-user", report.targetName());
    assertEquals("spamming", report.reason());
    assertEquals("PENDING", report.status());

    // ItemResponse
    ItemResponse itemRes = new ItemResponse(
        "i-1", "Name", "ART", 50.0, "NEW", "s-1", "desc", "url"
    );
    assertEquals("i-1", itemRes.id());
    assertEquals("Name", itemRes.name());
    assertEquals("ART", itemRes.itemType());
    assertEquals(50.0, itemRes.startingPrice());
    assertEquals("NEW", itemRes.condition());
    assertEquals("s-1", itemRes.sellerId());
    assertEquals("desc", itemRes.description());
    assertEquals("url", itemRes.imageUrl());

    // UserResponse
    UserResponse userRes = new UserResponse(
        "u-1", "user1", "user1@example.com", "User One", 100.0, 10.0, true, List.of("ALL")
    );
    assertEquals("u-1", userRes.id());
    assertEquals("user1", userRes.username());
    assertEquals("user1@example.com", userRes.email());
    assertEquals("User One", userRes.fullName());
    assertEquals(100.0, userRes.balance());
    assertEquals(10.0, userRes.holdBalance());
    assertTrue(userRes.isAdmin());
    assertEquals(List.of("ALL"), userRes.permissions());

    // NotificationResponse
    NotificationResponse notif = new NotificationResponse(
        "n-1", "INFO", "Hello", false, "2026-06-02T00:00:00"
    );
    assertEquals("n-1", notif.id());
    assertEquals("INFO", notif.type());
    assertEquals("Hello", notif.message());
    assertEquals(false, notif.read());
    assertEquals("2026-06-02T00:00:00", notif.createdAt());

    // AuctionResponse
    AuctionResponse aucRes = new AuctionResponse(
        "a-1", "Auction", "s-1", "Seller", "i-1", "Item", "b-1", "Bidder",
        10.0, 15.0, "ACTIVE", now, now.plusDays(1), "url", "desc", "ART", "spec"
    );
    assertEquals("a-1", aucRes.id());
    assertEquals("Auction", aucRes.title());
    assertEquals("s-1", aucRes.sellerId());
    assertEquals("Seller", aucRes.sellerName());
    assertEquals("i-1", aucRes.itemId());
    assertEquals("Item", aucRes.itemName());
    assertEquals("b-1", aucRes.leadingBidderId());
    assertEquals("Bidder", aucRes.leadingBidderName());
    assertEquals(10.0, aucRes.startingPrice());
    assertEquals(15.0, aucRes.currentPrice());
    assertEquals("ACTIVE", aucRes.status());
    assertEquals(now, aucRes.startTime());
    assertEquals(now.plusDays(1), aucRes.endTime());
    assertEquals("url", aucRes.imageUrl());
    assertEquals("desc", aucRes.itemDescription());
    assertEquals("ART", aucRes.itemType());
    assertEquals("spec", aucRes.specificInfo());

    // PageResponse
    PageResponse<String> page = new PageResponse<>(
        Collections.singletonList("item"), 1, 10, 100, 10, true
    );
    assertEquals(Collections.singletonList("item"), page.content());
    assertEquals(1, page.page());
    assertEquals(10, page.size());
    assertEquals(100, page.totalElements());
    assertEquals(10, page.totalPages());
    assertTrue(page.last());
  }
}
