package auction.exception;

/**
 * Thrown when: Non-admin attempts an admin-only operation.
If this admin lacks "CANCEL_AUCTION".
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
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
