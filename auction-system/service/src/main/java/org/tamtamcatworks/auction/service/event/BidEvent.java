package org.tamtamcatworks.auction.service.event;

/**
 * Spring application event published by {@link org.tamtamcatworks.auction.service.auction.BidService}
 * after a bid is accepted and recorded on the auction.
 *
 * <p>Consumed by:
 * <ul>
 *   <li>Teammate A — {@code NotificationService} to persist BID_PLACED / OUTBID notifications.</li>
 *   <li>Teammate B — {@code NotificationBroadcastListener} to broadcast live price over WebSocket.</li>
 * </ul>
 */
public record BidEvent(
    String auctionId,
    String auctionTitle,
    String sellerId,
    String bidderId,
    String previousLeaderId,  // null if this was the first bid
    double amount
) {}
