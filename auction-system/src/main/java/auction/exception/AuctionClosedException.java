package auction.exception;

/**
 * Thrown when: Action attempted on a non-ACTIVE auction.
If the auction is already CLOSED or CANCELLED.
Auction is not in PENDING state.
Auction is not ACTIVE.
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
public class AuctionClosedException extends RuntimeException {

    public AuctionClosedException(String message) {
        super(message);
    }
}
