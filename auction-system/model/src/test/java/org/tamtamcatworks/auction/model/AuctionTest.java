package org.tamtamcatworks.auction.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.Other;
import org.tamtamcatworks.auction.model.user.User;

/**
 * Unit tests for Auction entity and BidTransaction value object.
 */
public class AuctionTest {

  private User createTestUser(String username) {
    return new User(username, username + "@example.com", "hash", "Full Name", 10000.0);
  }

  private Other createTestItem(User seller) {
    return new Other("Mug", "Desc", 10.0, ItemCondition.NEW, "img", seller);
  }

  @Test
  public void testAuctionConstructorValidations() {
    User seller = createTestUser("seller");
    Other item = createTestItem(seller);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusHours(2);

    // Valid constructor
    Auction auction = new Auction("Auction Title", seller, item, 50.0, start, end);
    assertEquals("Auction Title", auction.getTitle());
    assertEquals(seller, auction.getSeller());
    assertEquals(item, auction.getItem());
    assertEquals(50.0, auction.getStartingPrice());
    assertEquals(50.0, auction.getCurrentPrice());
    assertEquals(start, auction.getStartTime());
    assertEquals(end, auction.getEndTime());
    assertEquals(AuctionStatus.PENDING, auction.getStatus());
    assertEquals(1000.0, auction.getMinimumIncrement());
    assertNull(auction.getLeadingBidder());
    assertTrue(auction.getBidHistory().isEmpty());

    // Invalid starting price
    assertThrows(IllegalArgumentException.class, () ->
        new Auction("Title", seller, item, 0.0, start, end));
    assertThrows(IllegalArgumentException.class, () ->
        new Auction("Title", seller, item, -10.0, start, end));

    // Invalid end time (same or before start time)
    assertThrows(IllegalArgumentException.class, () ->
        new Auction("Title", seller, item, 50.0, start, start));
    assertThrows(IllegalArgumentException.class, () ->
        new Auction("Title", seller, item, 50.0, start, start.minusSeconds(1)));
  }

  @Test
  public void testAuctionStateTransitions() {
    User seller = createTestUser("seller");
    Other item = createTestItem(seller);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusHours(2);
    Auction auction = new Auction("Auction Title", seller, item, 100.0, start, end);

    // Initial is PENDING
    assertFalse(auction.isAcceptingBids());
    assertFalse(auction.getStatus().isFinished());

    // Close and Cancel must fail on PENDING
    assertThrows(IllegalStateException.class, () -> auction.close());
    // Cancel should succeed on PENDING because cancel is allowed when status is not finished
    auction.cancel("No longer available");
    assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
    assertTrue(auction.getStatus().isFinished());

    // Creating new auction to test active -> closed transition
    Auction auction2 = new Auction("Auction Title 2", seller, item, 100.0, start, end);
    auction2.open();
    assertEquals(AuctionStatus.ACTIVE, auction2.getStatus());
    assertTrue(auction2.isAcceptingBids());
    assertFalse(auction2.getStatus().isFinished());

    // Cannot open active auction
    assertThrows(IllegalStateException.class, () -> auction2.open());

    // Close active auction
    auction2.close();
    assertEquals(AuctionStatus.CLOSED, auction2.getStatus());
    assertFalse(auction2.isAcceptingBids());
    assertTrue(auction2.getStatus().isFinished());

    // Cannot open closed auction
    assertThrows(IllegalStateException.class, () -> auction2.open());
    // Cannot close closed auction
    assertThrows(IllegalStateException.class, () -> auction2.close());
    // Cannot cancel closed auction
    assertThrows(IllegalStateException.class, () -> auction2.cancel("Finished"));

    // Creating new auction to test active -> cancelled transition
    Auction auction3 = new Auction("Auction Title 3", seller, item, 100.0, start, end);
    auction3.open();
    auction3.cancel("Seller request");
    assertEquals(AuctionStatus.CANCELLED, auction3.getStatus());
    assertTrue(auction3.getStatus().isFinished());
  }

  @Test
  public void testBidValidationAndRecording() {
    User seller = createTestUser("seller");
    Other item = createTestItem(seller);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusHours(2);
    Auction auction = new Auction("Auction Title", seller, item, 100.0, start, end);

    // Minimum increment is 1000.0
    // Current price is 100.0
    // Valid bid amount must be > 100.0 + 1000.0 = 1100.0
    assertTrue(auction.isValidBidAmount(1100.0));
    assertTrue(auction.isValidBidAmount(1200.0));
    assertFalse(auction.isValidBidAmount(1099.9));
    assertFalse(auction.isValidBidAmount(100.0));
    assertFalse(auction.isValidBidAmount(50.0));

    User bidder = createTestUser("bidder");
    BidTransaction bid = new BidTransaction(auction, bidder, 1100.0, BidTransaction.BidType.MANUAL);

    auction.recordBid(bid);
    assertEquals(1100.0, auction.getCurrentPrice());
    assertEquals(bidder, auction.getLeadingBidder());
    assertEquals(1, auction.getBidHistory().size());
    assertEquals(bid, auction.getBidHistory().get(0));

    // After updating currentPrice to 1100.0, new valid bid must be >= 1100.0 + 1000.0 = 2100.0
    assertTrue(auction.isValidBidAmount(2100.0));
    assertFalse(auction.isValidBidAmount(2099.0));

    // Test setter for minimum increment
    auction.setMinimumIncrement(500.0);
    assertEquals(500.0, auction.getMinimumIncrement());
    assertTrue(auction.isValidBidAmount(1600.0)); // 1100 + 500 = 1600
    assertFalse(auction.isValidBidAmount(1599.0));

    assertThrows(IllegalArgumentException.class, () -> auction.setMinimumIncrement(-10.0));
  }

  @Test
  public void testExtendEndTime() {
    User seller = createTestUser("seller");
    Other item = createTestItem(seller);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusHours(2);
    Auction auction = new Auction("Auction Title", seller, item, 100.0, start, end);

    auction.extendEndTime(600); // 10 minutes
    assertEquals(end.plusSeconds(600), auction.getEndTime());
  }

  @Test
  public void testBidTransactionValueObject() {
    User seller = createTestUser("seller");
    Other item = createTestItem(seller);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusHours(2);
    Auction auction = new Auction("Auction Title", seller, item, 100.0, start, end);
    User bidder = createTestUser("bidder");

    BidTransaction bidManual = new BidTransaction(auction, bidder, 1500.0, BidTransaction.BidType.MANUAL);
    assertEquals(auction, bidManual.getAuction());
    assertEquals(bidder, bidManual.getBidder());
    assertEquals(1500.0, bidManual.getAmount());
    assertEquals(BidTransaction.BidType.MANUAL, bidManual.getBidType());
    assertFalse(bidManual.isAutoBid());

    BidTransaction bidAuto = new BidTransaction(auction, bidder, 2000.0, BidTransaction.BidType.AUTO);
    assertTrue(bidAuto.isAutoBid());

    // Validation: amount <= 0
    assertThrows(IllegalArgumentException.class, () ->
        new BidTransaction(auction, bidder, 0.0, BidTransaction.BidType.MANUAL));
    assertThrows(IllegalArgumentException.class, () ->
        new BidTransaction(auction, bidder, -100.0, BidTransaction.BidType.MANUAL));

    // Test toString
    assertNotNull(bidManual.toString());
    assertNotNull(auction.toString());
  }
}
