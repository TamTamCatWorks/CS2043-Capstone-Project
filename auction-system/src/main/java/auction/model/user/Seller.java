package auction.model.user;

import auction.model.Auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Encapsulation & Invariant Enforcement                      │
 * │                                                                         │
 * │  Seller owns a list of active auctions and a rating. Both need          │
 * │  carefully controlled mutation: the list must never be directly         │
 * │  modified by callers, and the rating must stay within [0.0, 5.0].       │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. updateRating() must "clamp silently" — it never throws, it just    │
 * │      corrects out-of-range values. Implement the clamping using         │
 * │      Math.min/Math.max. Why is silent clamping preferred over throwing  │
 * │      an exception here?                                                 │
 * │                                                                         │
 * │  Q2. activeListings is modified internally (by AuctionManager) but      │
 * │      exposed read-only via getActiveListings(). What package-private     │
 * │      or friend-style technique could you use to let AuctionManager add  │
 * │      to the list without exposing the raw list to everyone?             │
 * │                                                                         │
 * │  Q3. Seller does NOT implement AuctionObserver, but Bidder does.        │
 * │      Should a Seller also receive notifications (e.g. "your item sold   │
 * │      for X")? How would you add that without changing the interface?    │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class Seller extends User {

    // TODO: declare activeListings (List<Auction>)
    // TODO: declare rating (double, range [0.0, 5.0], default 0.0)

    public Seller(String name, String email) {
        super(name, email);
        // TODO: initialise activeListings and rating
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns an unmodifiable view of the seller's current active auctions.
     */
    public List<Auction> getActiveListings() {
        // TODO: return Collections.unmodifiableList(activeListings)
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public double getRating() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Updates the seller's rating, clamping to [0.0, 5.0] silently.
     * Values below 0.0 become 0.0; values above 5.0 become 5.0.
     */
    public void updateRating(double newRating) {
        // TODO: clamp and assign
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Package-private helper used by AuctionManager to attach a new auction
     * to this seller's listing. External code should not call this directly.
     */
    void addListing(Auction auction) {
        // TODO: add to internal list
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getDisplayInfo() {
        // TODO: return formatted string, e.g. "Seller[name=..., rating=...]"
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
