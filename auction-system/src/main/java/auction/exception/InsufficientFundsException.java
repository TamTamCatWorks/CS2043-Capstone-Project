package auction.exception;

/**
 * Thrown when: Bidder's balance is lower than the bid amount.
If withdrawal amount exceeds current balance.
Bidder's balance &lt; amount.
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
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
