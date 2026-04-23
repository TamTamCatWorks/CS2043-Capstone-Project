package auction.model;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Finite-State Machine encoded as an Enum                   │
 * │                                                                         │
 * │  Legal transitions:                                                     │
 * │    PENDING  → ACTIVE    (via AuctionManager.startAuction)              │
 * │    ACTIVE   → CLOSED    (via AuctionManager.closeAuction)              │
 * │    PENDING/ACTIVE → CANCELLED (via Admin.cancelAuction)                │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. Add a method `boolean canTransitionTo(AuctionStatus next)` to this │
 * │      enum that returns true only for legal transitions above. Where     │
 * │      should the enforcement live — in the enum, in Auction, or in      │
 * │      AuctionManager? Justify your answer.                               │
 * │                                                                         │
 * │  Q2. CLOSED and CANCELLED are both terminal states. What does           │
 * │      "terminal" mean in a state machine? List ALL methods in the API    │
 * │      that must check for a terminal status before proceeding.           │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public enum AuctionStatus {
    PENDING,    // Created, not yet open for bids
    ACTIVE,     // Open for bidding
    CLOSED,     // Successfully closed; winner determined
    CANCELLED;  // Forcibly stopped by Admin; no winner

    /**
     * TODO (Q1): Implement canTransitionTo(AuctionStatus next).
     * Returns true only for legal state transitions.
     */
    public boolean canTransitionTo(AuctionStatus next) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Convenience: returns true if this is a terminal state (no further transitions). */
    public boolean isTerminal() {
        // TODO: return true for CLOSED and CANCELLED
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
