package auction.model.user;

import auction.exception.AuctionClosedException;
import auction.exception.UnauthorizedActionException;
import auction.model.Auction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Role-Based Access Control (RBAC) in OOP                   │
 * │                                                                         │
 * │  Admin uses a simple string-based permission list to gate privileged    │
 * │  operations. This is a micro-version of the RBAC pattern used in        │
 * │  real-world systems (Spring Security, AWS IAM, etc.).                   │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. hasPermission() must be case-insensitive. Which String method      │
 * │      achieves this? Why is case-insensitivity a good UX decision for    │
 * │      permission keys?                                                   │
 * │                                                                         │
 * │  Q2. The spec says granting a duplicate permission is "silently         │
 * │      ignored". How do you detect duplicates? What data structure        │
 * │      would make duplicate-detection O(1) instead of O(n)?              │
 * │      (You may use either; just justify your choice.)                    │
 * │                                                                         │
 * │  Q3. cancelAuction() must check the permission AND the auction status.  │
 * │      In what order should these checks happen, and why? Does order      │
 * │      matter for correctness? Does it matter for user-facing messages?   │
 * │                                                                         │
 * │  📌 SPRING POINTER                                                      │
 * │  In a Spring Security context, permissions like "CANCEL_AUCTION"        │
 * │  map to GrantedAuthority objects. The hasPermission() method here       │
 * │  mirrors how @PreAuthorize("hasAuthority('CANCEL_AUCTION')") works.    │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class Admin extends User {

    /** Default permissions granted to every new Admin. */
    private static final List<String> DEFAULT_PERMISSIONS =
            List.of("CANCEL_AUCTION", "REMOVE_USER", "VIEW_REPORTS");

    // TODO: declare permissions (mutable List<String>)

    /**
     * Creates an Admin with the full default permission set.
     */
    public Admin(String name, String email) {
        super(name, email);
        // TODO: initialise permissions from DEFAULT_PERMISSIONS (make a mutable copy!)
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns true if this admin holds the given permission key.
     * Comparison is case-insensitive.
     */
    public boolean hasPermission(String permission) {
        // TODO: implement (case-insensitive contains check)
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Adds a permission if not already present (silent on duplicate). */
    public void grantPermission(String permission) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Removes a permission if present (silent if not found). */
    public void revokePermission(String permission) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Forcibly cancels any auction regardless of seller.
     *
     * @throws UnauthorizedActionException if this admin lacks "CANCEL_AUCTION".
     * @throws AuctionClosedException      if the auction is already CLOSED or CANCELLED.
     */
    public void cancelAuction(Auction auction)
            throws UnauthorizedActionException, AuctionClosedException {
        // TODO: check permission first, then check auction status, then cancel
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getDisplayInfo() {
        // TODO: return formatted string, e.g. "Admin[name=..., permissions=...]"
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
