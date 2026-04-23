package auction.observer;

import auction.model.Auction;

/**
 * Immutable value object passed to every AuctionObserver.
 *
 * Fields:
 *   type    — what happened (see AuctionEventType)
 *   auction — the auction involved
 *   payload — the new BidTransaction for BID_PLACED, null otherwise
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  STUDENT QUESTION:                                                      │
 * │  The payload is typed as Object. Cast it to BidTransaction when         │
 * │  type == BID_PLACED. Could this ever ClassCastException at runtime?     │
 * │  What defensive coding practice prevents it?                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class AuctionEvent {

    // TODO: declare type    (AuctionEventType, final)
    // TODO: declare auction (Auction, final)
    // TODO: declare payload (Object, final — BidTransaction or null)

    public AuctionEvent(AuctionEventType type, Auction auction, Object payload) {
        // TODO: assign fields
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public AuctionEventType getType()    { throw new UnsupportedOperationException("Not yet implemented"); }
    public Auction          getAuction() { throw new UnsupportedOperationException("Not yet implemented"); }
    public Object           getPayload() { throw new UnsupportedOperationException("Not yet implemented"); }
}
