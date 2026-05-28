package org.tamtamcatworks.auction.service.event;

import org.tamtamcatworks.auction.model.AuctionStatus;

/**
 * Spring application event published by {@link org.tamtamcatworks.auction.service.auction.AuctionService}
 * whenever an auction's status changes (OPENED, CLOSED, CANCELLED).
 *
 * <p>Consumed by:
 * <ul>
 *   <li>Teammate A — {@code NotificationService} to persist Notification rows.</li>
 *   <li>Teammate B — {@code NotificationBroadcastListener} to push via WebSocket.</li>
 * </ul>
 */
public record AuctionEvent(
    String auctionId,
    String auctionTitle,
    String sellerId,
    String leadingBidderId,
    double currentPrice,
    AuctionStatus newStatus,
    String reason   // non-null only when CANCELLED
) {}
