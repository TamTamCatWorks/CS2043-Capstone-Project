package auction.model;

import auction.model.user.Bidder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Value Objects / Immutable Records                          │
 * │                                                                         │
 * │  BidTransaction is a pure value object — once created it never          │
 * │  changes. It records the facts of a single accepted bid: who, how       │
 * │  much, on which auction, and when.                                      │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. The spec says the constructor is "package-private" — only          │
 * │      AuctionManager.placeBid() should create instances. But             │
 * │      BidTransaction is in the `auction.model` package while             │
 * │      AuctionManager is in `auction.manager`. Does package-private work  │
 * │      across packages? How would you solve this? (Options: make it       │
 * │      package-private by moving AuctionManager, use a factory method,    │
 * │      or keep it public. Justify your choice.)                           │
 * │                                                                         │
 * │  Q2. Java 16+ introduced `record` types which are perfect for value     │
 * │      objects. Rewrite BidTransaction as a `record`. What do you gain    │
 * │      and lose vs the regular class approach?                            │
 * │                                                                         │
 * │  Q3. transactionId is auto-generated. Could two simultaneous bids       │
 * │      (hypothetically, in a multi-threaded scenario) produce the same    │
 * │      UUID? How does UUID.randomUUID() protect against this?             │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class BidTransaction {

    // TODO: declare transactionId (String UUID, auto-generated)
    // TODO: declare auction       (Auction, immutable reference)
    // TODO: declare bidder        (Bidder, immutable reference)
    // TODO: declare amount        (double, immutable)
    // TODO: declare timestamp     (LocalDateTime, immutable)

    /**
     * All fields are immutable. Only AuctionManager.placeBid() should call this.
     *
     * @param auction   The auction this bid belongs to.
     * @param bidder    The bidder who placed the bid.
     * @param amount    The accepted bid amount.
     * @param timestamp The exact time the bid was accepted.
     */
    public BidTransaction(Auction auction, Bidder bidder, double amount, LocalDateTime timestamp) {
        // TODO: generate transactionId UUID
        // TODO: assign all fields
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String        getTransactionId() { throw new UnsupportedOperationException("Not yet implemented"); }
    public Auction       getAuction()       { throw new UnsupportedOperationException("Not yet implemented"); }
    public Bidder        getBidder()        { throw new UnsupportedOperationException("Not yet implemented"); }
    public double        getAmount()        { throw new UnsupportedOperationException("Not yet implemented"); }
    public LocalDateTime getTimestamp()     { throw new UnsupportedOperationException("Not yet implemented"); }
}
