package org.tamtamcatworks.auction.api.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.tamtamcatworks.auction.service.event.AuctionEvent;
import org.tamtamcatworks.auction.service.event.BidEvent;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class NotificationBroadcastListener {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationBroadcastListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast new bid price to all subscribers watching this auction.
     * Topic: /topic/auctions/{auctionId}
     */
    @EventListener
    public void onBidPlaced(BidEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/auctions/" + event.auctionId(),
                new AuctionPriceUpdate(event.auctionId(), event.amount(), LocalDateTime.now().toString())
        );

        // Push personal notification to outbid user
        if (event.previousLeaderId() != null) {
            messagingTemplate.convertAndSendToUser(
                    event.previousLeaderId(),
                    "/queue/notifications",
                    new NotificationResponse(
                            UUID.randomUUID().toString(),
                            "OUTBID",
                            String.format("You've been outbid on \"%s\". New price: $%.2f",
                                    event.auctionTitle(), event.amount()),
                            false,
                            LocalDateTime.now().toString()
                    )
            );
        }
    }

    /**
     * Broadcast auction status change.
     * Topic: /topic/auctions/{auctionId}/status
     */
    @EventListener
    public void onAuctionStatusChanged(AuctionEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/auctions/" + event.auctionId() + "/status",
                new AuctionStatusUpdate(event.auctionId(), event.newStatus().name(), event.reason())
        );
    }

    // ── Inner payload records ──────────────────────────────────────────────────
    public record AuctionPriceUpdate(String auctionId, double newPrice, String timestamp) {}
    public record AuctionStatusUpdate(String auctionId, String newStatus, String reason) {}
}
