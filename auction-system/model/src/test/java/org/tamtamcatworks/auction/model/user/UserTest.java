package org.tamtamcatworks.auction.model.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for User entity. */
public class UserTest {

  @Test
  public void testConstructorAndGetters() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);

    assertEquals("johndoe", user.getUsername());
    assertEquals("john@example.com", user.getEmail());
    assertEquals("hashed_pass", user.getPasswordHash());
    assertEquals("John Doe", user.getFullName());
    assertEquals(5000.0, user.getBalance());
    assertEquals(0.0, user.getHoldBalance());
  }

  @Test
  public void testAddBalance() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);

    user.addBalance(1500.0);
    assertEquals(6500.0, user.getBalance());

    // Validation: amount must be > 0
    assertThrows(IllegalArgumentException.class, () -> user.addBalance(0.0));
    assertThrows(IllegalArgumentException.class, () -> user.addBalance(-100.0));
  }

  @Test
  public void testDeductBalance() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);

    user.deductBalance(2000.0);
    assertEquals(3000.0, user.getBalance());

    // Validation: amount must be > 0
    assertThrows(IllegalArgumentException.class, () -> user.deductBalance(0.0));
    assertThrows(IllegalArgumentException.class, () -> user.deductBalance(-100.0));

    // Validation: insufficient balance
    assertThrows(IllegalArgumentException.class, () -> user.deductBalance(3000.01));
  }

  @Test
  public void testRefundBalance() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);

    user.refundBalance(1000.0);
    assertEquals(6000.0, user.getBalance());

    // Validation: amount must be > 0
    assertThrows(IllegalArgumentException.class, () -> user.refundBalance(0.0));
    assertThrows(IllegalArgumentException.class, () -> user.refundBalance(-100.0));
  }

  @Test
  public void testHoldFunds() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);

    user.holdFunds(2000.0);
    assertEquals(3000.0, user.getBalance());
    assertEquals(2000.0, user.getHoldBalance());

    // Validation: amount must be > 0
    assertThrows(IllegalArgumentException.class, () -> user.holdFunds(0.0));
    assertThrows(IllegalArgumentException.class, () -> user.holdFunds(-100.0));

    // Validation: insufficient balance
    assertThrows(IllegalArgumentException.class, () -> user.holdFunds(3000.01));
  }

  @Test
  public void testReleaseHeldFunds() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);
    user.holdFunds(2000.0);

    user.releaseHeldFunds(1500.0);
    assertEquals(4500.0, user.getBalance());
    assertEquals(500.0, user.getHoldBalance());

    // Validation: amount must be > 0
    assertThrows(IllegalArgumentException.class, () -> user.releaseHeldFunds(0.0));
    assertThrows(IllegalArgumentException.class, () -> user.releaseHeldFunds(-100.0));

    // Validation: insufficient held balance
    assertThrows(IllegalArgumentException.class, () -> user.releaseHeldFunds(500.01));
  }

  @Test
  public void testConsumeHeldFunds() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);
    user.holdFunds(2000.0);

    user.consumeHeldFunds(1500.0);
    assertEquals(3000.0, user.getBalance());
    assertEquals(500.0, user.getHoldBalance());

    // Validation: amount must be > 0
    assertThrows(IllegalArgumentException.class, () -> user.consumeHeldFunds(0.0));
    assertThrows(IllegalArgumentException.class, () -> user.consumeHeldFunds(-100.0));

    // Validation: insufficient held balance
    assertThrows(IllegalArgumentException.class, () -> user.consumeHeldFunds(500.01));
  }

  @Test
  public void testProfiles() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);

    assertNull(user.getBuyerProfile());
    assertNull(user.getSellerProfile());
    assertNull(user.getAdminProfile());

    BuyerProfile buyer = new BuyerProfile();
    SellerProfile seller = new SellerProfile();
    AdminProfile admin = new AdminProfile();

    user.setBuyerProfile(buyer);
    user.setSellerProfile(seller);
    user.setAdminProfile(admin);

    assertEquals(buyer, user.getBuyerProfile());
    assertEquals(seller, user.getSellerProfile());
    assertEquals(admin, user.getAdminProfile());
  }

  @Test
  public void testBuyerProfile() {
    BuyerProfile buyer = new BuyerProfile();
    assertEquals(0, buyer.getTotalWins());
    assertEquals(0.0, buyer.getTotalSpent());
    assertTrue(buyer.getBiddingHistory().isEmpty());
    assertTrue(buyer.getWatchlist().isEmpty());

    buyer.addToBiddingHistory("auc-1");
    buyer.addToBiddingHistory("auc-1"); // Duplicate check
    assertEquals(1, buyer.getBiddingHistory().size());
    assertEquals("auc-1", buyer.getBiddingHistory().get(0));

    buyer.addToWatchlist("auc-2");
    buyer.addToWatchlist("auc-2"); // Duplicate check
    assertEquals(1, buyer.getWatchlist().size());
    assertEquals("auc-2", buyer.getWatchlist().get(0));

    buyer.removeFromWatchlist("auc-2");
    assertTrue(buyer.getWatchlist().isEmpty());

    buyer.addToWatchlist("auc-3");
    buyer.recordWin("auc-3", 1500.0);
    assertEquals(1, buyer.getTotalWins());
    assertEquals(1500.0, buyer.getTotalSpent());
    assertTrue(buyer.getWatchlist().isEmpty()); // Removed on win

    // Test setters
    buyer.setTotalWins(5);
    buyer.setTotalSpent(2000.0);
    buyer.setBiddingHistory(java.util.List.of("a", "b"));
    buyer.setWatchlist(java.util.List.of("c"));
    assertEquals(5, buyer.getTotalWins());
    assertEquals(2000.0, buyer.getTotalSpent());
    assertEquals(2, buyer.getBiddingHistory().size());
    assertEquals(1, buyer.getWatchlist().size());

    // Test toString
    assertNotNull(buyer.toString());
  }

  @Test
  public void testSellerProfile() {
    SellerProfile seller = new SellerProfile();
    assertEquals(0.0, seller.getRating());
    assertEquals(0, seller.getRatingCount());
    assertEquals(0.0, seller.getTotalRevenue());
    assertEquals(0, seller.getTotalSold());
    assertTrue(seller.getListings().isEmpty());

    seller.addListing("auc-1");
    seller.addListing("auc-1"); // Duplicate check
    assertEquals(1, seller.getListings().size());
    assertEquals("auc-1", seller.getListings().get(0));

    seller.recordSale("auc-1", 5000.0);
    assertEquals(1, seller.getTotalSold());
    assertEquals(5000.0, seller.getTotalRevenue());

    seller.addRating(4.0);
    assertEquals(4.0, seller.getRating());
    assertEquals(1, seller.getRatingCount());

    seller.addRating(5.0);
    assertEquals(4.5, seller.getRating());
    assertEquals(2, seller.getRatingCount());

    assertThrows(IllegalArgumentException.class, () -> seller.addRating(0.9));
    assertThrows(IllegalArgumentException.class, () -> seller.addRating(5.1));

    // Test setters
    seller.setRating(3.5);
    seller.setRatingCount(10);
    seller.setTotalRevenue(10000.0);
    seller.setTotalSold(3);
    seller.setListings(java.util.List.of("a"));
    assertEquals(3.5, seller.getRating());
    assertEquals(10, seller.getRatingCount());
    assertEquals(10000.0, seller.getTotalRevenue());
    assertEquals(3, seller.getTotalSold());
    assertEquals(1, seller.getListings().size());

    assertNotNull(seller.toString());
  }

  @Test
  public void testAdminProfile() {
    AdminProfile admin = new AdminProfile(java.util.List.of("MANAGE_USERS"));
    assertTrue(admin.hasPermission("MANAGE_USERS"));
    assertFalse(admin.hasPermission("MANAGE_ADMINS"));
    assertEquals(1, admin.getPermissions().size());

    admin.grantPermission("MANAGE_ITEMS");
    assertTrue(admin.hasPermission("MANAGE_ITEMS"));
    assertEquals(2, admin.getPermissions().size());
    assertEquals(1, admin.getActionLog().size());

    admin.revokePermission("MANAGE_USERS");
    assertFalse(admin.hasPermission("MANAGE_USERS"));
    assertEquals(2, admin.getActionLog().size());

    admin.setPermissions(java.util.List.of("VIEW_LOGS"));
    assertTrue(admin.hasPermission("VIEW_LOGS"));
    assertFalse(admin.hasPermission("MANAGE_ITEMS"));

    AdminProfile superAdmin = AdminProfile.superAdmin();
    assertTrue(superAdmin.hasPermission("MANAGE_USERS"));
    assertTrue(superAdmin.hasPermission("MANAGE_ITEMS"));
    assertTrue(superAdmin.hasPermission("MANAGE_AUCTIONS"));
    assertTrue(superAdmin.hasPermission("VIEW_LOGS"));
    assertTrue(superAdmin.hasPermission("MANAGE_ADMINS"));

    assertNotNull(superAdmin.toString());
  }

  @Test
  public void testToString() {
    User user = new User("johndoe", "john@example.com", "hashed_pass", "John Doe", 5000.0);
    String str = user.toString();
    assertTrue(str.contains("User: johndoe"));
    assertTrue(str.contains("Balance: 5000.0"));
  }
}
