package auction.exception;

/**
 * Thrown when: Bid amount ≤ current highest bid, or bidder is the seller.
Amount ≤ current high bid, or bidder is the seller.
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
public class InvalidBidException extends RuntimeException {

    public InvalidBidException(String message) {
        super(message);
    }
}
