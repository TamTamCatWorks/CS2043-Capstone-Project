package auction.observer;

/**
 * Enum of all event types that AuctionManager can broadcast.
 */
public enum AuctionEventType {
    AUCTION_STARTED,
    BID_PLACED,
    AUCTION_CLOSED,
    AUCTION_CANCELLED
}
