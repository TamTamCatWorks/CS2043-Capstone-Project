package auction.exception;

/**
 * Thrown when: Lookup by auctionId returns no result.
No auction with that ID exists.
 *
 * All custom exceptions extend RuntimeException and accept a String message.
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  STUDENT QUESTION:                                                   │
 * │  Why do all custom exceptions in this project extend RuntimeException │
 * │  (unchecked) rather than Exception (checked)? What would change in   │
 * │  AuctionManager.placeBid() if InvalidBidException were checked?      │
 * └──────────────────────────────────────────────────────────────────────┘
 */
public class AuctionNotFoundException extends RuntimeException {

    public AuctionNotFoundException(String message) {
        super(message);
    }
}
