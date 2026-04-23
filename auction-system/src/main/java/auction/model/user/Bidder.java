package auction.model.user;

import auction.exception.InsufficientFundsException;
import auction.model.Auction;
import auction.observer.AuctionEvent;
import auction.observer.AuctionObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Interface Implementation & the Observer Pattern            │
 * │                                                                         │
 * │  Bidder is both a domain object (extends User) AND a listener           │
 * │  (implements AuctionObserver). This dual role is the core of the        │
 * │  Observer pattern: Bidder subscribes to events and reacts when          │
 * │  AuctionManager calls onAuctionEvent().                                 │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. A class can extend only one class but implement many interfaces.   │
 * │      Why is AuctionObserver an interface rather than an abstract class? │
 * │      What would break if it were an abstract class?                     │
 * │                                                                         │
 * │  Q2. getBidHistory() returns an unmodifiable view. Why not just return  │
 * │      the raw ArrayList? What bad things could a caller do if you did?   │
 * │      (Hint: think about Collections.unmodifiableList.)                  │
 * │                                                                         │
 * │  Q3. When onAuctionEvent() is called, the Bidder receives an            │
 * │      AuctionEvent. What information would a Bidder typically want to    │
 * │      act on? Write pseudocode describing what your implementation does  │
 * │      for each AuctionEventType.                                         │
 * │                                                                         │
 * │  📌 JAVAFX POINTER                                                      │
 * │  onAuctionEvent() is called from AuctionManager (the "model" layer).   │
 * │  In a JavaFX application this means it may be called on a background   │
 * │  thread. Any UI update must be wrapped in:                              │
 * │      Platform.runLater(() -> { /* update Label / TableView etc. *\/ }); │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class Bidder extends User implements AuctionObserver {

    // TODO: declare balance (double, must be >= 0 at all times)
    // TODO: declare bidHistory (List<BidTransaction>, internal list)

    /**
     * @param name           Non-null, non-empty display name.
     * @param email          Unique email address.
     * @param initialBalance Must be >= 0; throws IllegalArgumentException otherwise.
     */
    public Bidder(String name, String email, double initialBalance) {
        super(name, email);
        // TODO: validate initialBalance >= 0
        // TODO: initialise balance and bidHistory
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Returns current available balance. */
    public double getBalance() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Adds {@code amount} to the balance.
     * @throws IllegalArgumentException if amount <= 0.
     */
    public void deposit(double amount) {
        // TODO: validate then add
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Deducts {@code amount} from the balance.
     * @throws IllegalArgumentException      if amount <= 0.
     * @throws InsufficientFundsException    if amount > current balance.
     */
    public void withdraw(double amount) throws InsufficientFundsException {
        // TODO: validate then deduct
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns an unmodifiable view of all bids ever placed by this bidder.
     * The caller must NOT be able to add or remove entries via the returned list.
     */
    public List<auction.model.BidTransaction> getBidHistory() {
        // TODO: return Collections.unmodifiableList(bidHistory)
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Called by AuctionManager whenever a relevant auction event occurs.
     * Must NOT throw checked exceptions.
     *
     * Suggested behaviour:
     *  - BID_PLACED   → log/print the new highest bid
     *  - AUCTION_CLOSED → check if this bidder won; update internal state
     *  - AUCTION_CANCELLED → notify that the auction was cancelled
     */
    @Override
    public void onAuctionEvent(AuctionEvent event) {
        // TODO: implement reaction logic
        // TODO (JavaFX): wrap any UI updates in Platform.runLater(...)
    }

    @Override
    public String getDisplayInfo() {
        // TODO: return formatted string, e.g. "Bidder[name=..., balance=...]"
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
