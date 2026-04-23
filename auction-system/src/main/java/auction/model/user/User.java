package auction.model.user;

import auction.model.Entity;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Abstract Class — Intermediate Layer of Hierarchy           │
 * │                                                                         │
 * │  User sits between Entity (root) and concrete user types (Bidder,       │
 * │  Seller, Admin). It adds identity information that all users share.     │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. User extends Entity but is still abstract. Draw the full           │
 * │      inheritance chain from Entity down to Bidder. How many levels      │
 * │      deep is it? What is the tradeoff of deep vs shallow hierarchies?   │
 * │                                                                         │
 * │  Q2. Why is email described as a "login key"? What data structure       │
 * │      would you use in AuctionManager to look up a user by email         │
 * │      efficiently?                                                       │
 * │                                                                         │
 * │  Q3. isActive defaults to true. Where should this default be set —      │
 * │      in the field declaration, the constructor, or both? What           │
 * │      difference does it make?                                           │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public abstract class User extends Entity {

    // TODO: declare name   (String, non-null, non-empty)
    // TODO: declare email  (String, unique)
    // TODO: declare isActive (boolean, default true)

    /**
     * TODO: Constructor — initialise name, email, isActive.
     * Validate that name and email are non-null and non-empty.
     * Call super() to trigger Entity's UUID/timestamp generation.
     */
    protected User(String name, String email) {
        super();
        // TODO: validate and assign fields
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getName()  { throw new UnsupportedOperationException("Not yet implemented"); }
    public String getEmail() { throw new UnsupportedOperationException("Not yet implemented"); }
    public boolean isActive(){ throw new UnsupportedOperationException("Not yet implemented"); }

    /**
     * Enables or disables the account without deleting it.
     * A disabled account should be rejected by AuctionManager operations.
     */
    public void setActive(boolean active) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
