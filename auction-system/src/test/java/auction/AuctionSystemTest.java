package auction;

import auction.exception.*;
import auction.factory.ItemFactory;
import auction.manager.AuctionManager;
import auction.model.*;
import auction.model.item.*;
import auction.model.user.*;
import auction.observer.AuctionEvent;
import auction.observer.AuctionObserver;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Unit Testing with JUnit 5                                 │
 * │                                                                         │
 * │  These tests are the autograder scenarios from the API spec, written    │
 * │  as proper JUnit 5 test methods. They validate your implementation      │
 * │  without revealing it.                                                  │
 * │                                                                         │
 * │  Key annotations:                                                       │
 * │    @Test          — marks a test method                                 │
 * │    @BeforeEach    — runs before every test (used here for reset())      │
 * │    @DisplayName   — human-readable label in test reports                │
 * │    assertThrows() — verifies the correct exception type is thrown       │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. @BeforeEach calls manager.reset(). Why is this necessary between   │
 * │      tests? What could go wrong if two tests share the same             │
 * │      AuctionManager state?                                              │
 * │                                                                         │
 * │  Q2. assertThrows(ExceptionType.class, () -> { ... }) verifies both    │
 * │      the type of exception AND that the lambda throws. Write a version  │
 * │      using a try/catch block to understand what assertThrows replaces.  │
 * │                                                                         │
 * │  Q3. These tests are "black-box" — they test behaviour via the public   │
 * │      API only. Write one additional "white-box" test that directly       │
 * │      inspects internal state (e.g. the size of the bids list on an      │
 * │      Auction after two bids). What tradeoff does white-box testing      │
 * │      introduce?                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@DisplayName("Auction System — Autograder Scenarios")
class AuctionSystemTest {

    private AuctionManager manager;

    @BeforeEach
    void setUp() {
        manager = AuctionManager.getInstance();
        manager.reset();   // always start from a clean slate
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Scenario 01 — Singleton identity
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 01 — getInstance() returns the same object reference")
    void testSingletonIdentity() {
        AuctionManager m1 = AuctionManager.getInstance();
        AuctionManager m2 = AuctionManager.getInstance();
        assertSame(m1, m2, "Two calls to getInstance() must return the identical reference");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Scenario 02 — ItemFactory polymorphic dispatch
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 02 — ItemFactory creates correct subclasses and getCategoryDetails() works")
    void testItemFactoryAndPolymorphism() {
        Item laptop = ItemFactory.createItem(
                ItemType.ELECTRONICS,
                "Gaming Laptop", "High-end GPU", 1200.0, ItemCondition.LIKE_NEW,
                "Asus", 12, "ROG-G15"
        );
        assertInstanceOf(Electronics.class, laptop,
                "ItemFactory should return an Electronics instance for ELECTRONICS type");

        Item painting = ItemFactory.createItem(
                ItemType.ART,
                "Sunset", "Landscape oil painting", 5000.0, ItemCondition.GOOD,
                "Mai Thu", 1972, "Oil on canvas", true
        );
        assertInstanceOf(Art.class, painting,
                "ItemFactory should return an Art instance for ART type");

        Item car = ItemFactory.createItem(
                ItemType.VEHICLE,
                "Sedan", "Well maintained", 8000.0, ItemCondition.GOOD,
                "Honda", "Civic", 2018, 62000
        );
        assertInstanceOf(Vehicle.class, car,
                "ItemFactory should return a Vehicle instance for VEHICLE type");

        // Polymorphic dispatch — getCategoryDetails() and estimatedValue() vary by type
        for (Item item : List.of(laptop, painting, car)) {
            assertNotNull(item.getCategoryDetails(), "getCategoryDetails() must not return null");
            assertTrue(item.estimatedValue() > 0,   "estimatedValue() must be positive");
        }
    }

    @Test
    @DisplayName("Scenario 02b — ItemFactory throws IllegalArgumentException on bad args")
    void testItemFactoryInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () ->
                ItemFactory.createItem(null, "name", "desc", 100.0, ItemCondition.NEW),
                "Null ItemType should throw IllegalArgumentException"
        );
        assertThrows(IllegalArgumentException.class, () ->
                ItemFactory.createItem(ItemType.ELECTRONICS, "name", "desc", -1.0, ItemCondition.NEW,
                        "Brand", 0, "M1"),
                "Negative basePrice should throw IllegalArgumentException"
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Scenario 03 — Full auction lifecycle
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 03 — create → start → bid → close with correct balance transfers")
    void testFullAuctionLifecycle() {
        Seller alice = new Seller("Alice", "alice@example.com");
        Bidder bob   = new Bidder("Bob",   "bob@example.com",   2000.0);
        Bidder carol = new Bidder("Carol", "carol@example.com", 3000.0);

        Item laptop = ItemFactory.createItem(
                ItemType.ELECTRONICS, "Laptop", "Gaming", 800.0, ItemCondition.NEW,
                "Dell", 24, "XPS-15"
        );

        Auction auction = manager.createAuction(
                laptop, alice, 900.0,
                LocalDateTime.now().plusHours(2)
        );
        assertEquals(AuctionStatus.PENDING, auction.getStatus(),
                "Newly created auction must be PENDING");

        manager.registerObserver(bob);
        manager.registerObserver(carol);

        manager.startAuction(auction.getAuctionId());
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus(),
                "After startAuction(), status must be ACTIVE");

        BidTransaction t1 = manager.placeBid(auction.getAuctionId(), bob,   950.0);
        BidTransaction t2 = manager.placeBid(auction.getAuctionId(), carol, 1100.0);

        assertNotNull(t1, "placeBid() must return a non-null BidTransaction");
        assertNotNull(t2, "placeBid() must return a non-null BidTransaction");

        assertTrue(auction.getHighestBidder().isPresent(),
                "getHighestBidder() must be present after bids");
        assertSame(carol, auction.getHighestBidder().get(),
                "Carol placed the highest bid and should be the highest bidder");
        assertEquals(1100.0, auction.getCurrentHighestBid(), 0.001,
                "Current highest bid must equal Carol's bid of 1100.0");

        manager.closeAuction(auction.getAuctionId());

        assertEquals(AuctionStatus.CLOSED, auction.getStatus(),
                "After closeAuction(), status must be CLOSED");
        assertEquals(2000.0, bob.getBalance(), 0.001,
                "Bob's 950.0 bid must be fully refunded on close");
        assertEquals(3000.0 - 1100.0, carol.getBalance(), 0.001,
                "Carol's winning bid of 1100.0 must be deducted from her balance");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Scenario 04 — Exception paths
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 04a — Bid below current high throws InvalidBidException")
    void testBidBelowCurrentHigh() {
        Auction auction = createAndStartAuction(900.0);
        Bidder bob = new Bidder("Bob", "bob@example.com", 5000.0);
        manager.placeBid(auction.getAuctionId(), bob, 1000.0);

        // now try to bid lower
        Bidder carol = new Bidder("Carol", "carol@example.com", 5000.0);
        assertThrows(InvalidBidException.class, () ->
                manager.placeBid(auction.getAuctionId(), carol, 800.0),
                "Bid amount <= current high must throw InvalidBidException"
        );
    }

    @Test
    @DisplayName("Scenario 04b — Bidder with insufficient balance throws InsufficientFundsException")
    void testInsufficientFunds() {
        Auction auction = createAndStartAuction(100.0);
        Bidder broke = new Bidder("Broke", "broke@example.com", 10.0);

        assertThrows(InsufficientFundsException.class, () ->
                manager.placeBid(auction.getAuctionId(), broke, 500.0),
                "Bidder without enough funds must throw InsufficientFundsException"
        );
    }

    @Test
    @DisplayName("Scenario 04c — Bidding on a closed auction throws AuctionClosedException")
    void testBidOnClosedAuction() {
        Auction auction = createAndStartAuction(900.0);
        manager.closeAuction(auction.getAuctionId());

        Bidder bob = new Bidder("Bob", "bob@example.com", 5000.0);
        assertThrows(AuctionClosedException.class, () ->
                manager.placeBid(auction.getAuctionId(), bob, 1200.0),
                "Bidding on a CLOSED auction must throw AuctionClosedException"
        );
    }

    @Test
    @DisplayName("Scenario 04d — Seller cannot bid on their own auction")
    void testSellerCannotBidOnOwnAuction() {
        // Alice is the seller; aliceAsBidder shares the same email
        Seller alice = new Seller("Alice", "alice@example.com");
        Item item = ItemFactory.createItem(
                ItemType.VEHICLE, "Car", "desc", 500.0, ItemCondition.GOOD,
                "Toyota", "Yaris", 2020, 30000
        );
        Auction auction = manager.createAuction(item, alice, 600.0,
                LocalDateTime.now().plusHours(1));
        manager.startAuction(auction.getAuctionId());

        Bidder aliceAsBidder = new Bidder("Alice", "alice@example.com", 9999.0);
        assertThrows(InvalidBidException.class, () ->
                manager.placeBid(auction.getAuctionId(), aliceAsBidder, 700.0),
                "Seller bidding on their own auction must throw InvalidBidException"
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Scenario 05 — Admin cancel with permission check
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 05a — Admin with CANCEL_AUCTION permission can cancel an auction")
    void testAdminCancelWithPermission() {
        Auction auction = createAndStartAuction(500.0);
        Admin sysAdmin = new Admin("Sys", "sys@example.com");

        assertTrue(sysAdmin.hasPermission("CANCEL_AUCTION"),
                "A freshly created Admin must have CANCEL_AUCTION permission");

        sysAdmin.cancelAuction(auction);

        assertEquals(AuctionStatus.CANCELLED, auction.getStatus(),
                "Auction must be CANCELLED after admin.cancelAuction()");
    }

    @Test
    @DisplayName("Scenario 05b — Admin without CANCEL_AUCTION permission throws UnauthorizedActionException")
    void testAdminCancelWithoutPermission() {
        Auction auction = createAndStartAuction(500.0);
        Admin limitedAdmin = new Admin("Junior", "jr@example.com");
        limitedAdmin.revokePermission("CANCEL_AUCTION");

        assertThrows(UnauthorizedActionException.class, () ->
                limitedAdmin.cancelAuction(auction),
                "Admin without CANCEL_AUCTION must throw UnauthorizedActionException"
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper — creates, starts, and returns an auction for reuse
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates a simple active auction with the given starting price.
     * The seller's email is unique per call to avoid DuplicateEntityException.
     */
    private Auction createAndStartAuction(double startingPrice) {
        Seller seller = new Seller("TestSeller", "seller_" + System.nanoTime() + "@test.com");
        Item item = ItemFactory.createItem(
                ItemType.ELECTRONICS, "Widget", "A widget", startingPrice, ItemCondition.GOOD,
                "Acme", 0, "WGT-001"
        );
        Auction auction = manager.createAuction(
                item, seller, startingPrice,
                LocalDateTime.now().plusHours(1)
        );
        manager.startAuction(auction.getAuctionId());
        return auction;
    }
}
