package org.tamtamcatworks.auction.service.notification;

import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tamtamcatworks.auction.model.notification.Notification;
import org.tamtamcatworks.auction.model.notification.NotificationType;
import org.tamtamcatworks.auction.persist.repository.NotificationRepository;
import org.tamtamcatworks.auction.service.event.AuctionEvent;
import org.tamtamcatworks.auction.service.event.BidEvent;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

@Service
public class NotificationService {

  private final NotificationRepository repo;

  public NotificationService(NotificationRepository repo) {
    this.repo = repo;
  }

  // ── Event Listeners ──────────────────────────────────────────────────────

  @EventListener
  @Transactional
  public void onBidPlaced(BidEvent event) {
    // Notify seller
    repo.save(
        new Notification(
            event.sellerId(),
            NotificationType.BID_PLACED,
            String.format(
                "New bid of $%.2f placed on your auction \"%s\"",
                event.amount(), event.auctionTitle())));
    // Notify previous leader they've been outbid
    if (event.previousLeaderId() != null) {
      repo.save(
          new Notification(
              event.previousLeaderId(),
              NotificationType.OUTBID,
              String.format(
                  "You've been outbid on \"%s\". New price: $%.2f",
                  event.auctionTitle(), event.amount())));
    }
  }

  @EventListener
  @Transactional
  public void onAuctionStatusChanged(AuctionEvent event) {
    NotificationType type =
        switch (event.newStatus()) {
          case ACTIVE -> NotificationType.AUCTION_OPENED;
          case CLOSED -> NotificationType.AUCTION_CLOSED;
          case CANCELLED -> NotificationType.AUCTION_CANCELLED;
          default -> null;
        };
    if (type == null) return;

    String msg =
        switch (type) {
          case AUCTION_OPENED -> "Your auction \"" + event.auctionTitle() + "\" is now live!";
          case AUCTION_CLOSED -> "Your auction \"" + event.auctionTitle() + "\" has closed.";
          case AUCTION_CANCELLED -> "Auction \""
              + event.auctionTitle()
              + "\" was cancelled: "
              + event.reason();
          default -> "";
        };
    repo.save(new Notification(event.sellerId(), type, msg));

    // Notify winner (leading bidder) upon closure
    if (type == NotificationType.AUCTION_CLOSED && event.leadingBidderId() != null) {
      repo.save(
          new Notification(
              event.leadingBidderId(),
              NotificationType.AUCTION_CLOSED,
              String.format(
                  "Congratulations! You won the auction \"%s\" for $%.2f.",
                  event.auctionTitle(), event.currentPrice())));
    }

    // Notify leading bidder upon cancellation
    if (type == NotificationType.AUCTION_CANCELLED && event.leadingBidderId() != null) {
      repo.save(
          new Notification(
              event.leadingBidderId(),
              NotificationType.AUCTION_CANCELLED,
              String.format(
                  "The auction \"%s\" was cancelled. Your bid of $%.2f has been refunded.",
                  event.auctionTitle(), event.currentPrice())));
    }
  }

  // ── Query Methods ─────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<NotificationResponse> getForUser(String userId) {
    return repo.findByUserIdOrderByCreationDateDesc(userId).stream()
        .map(
            n ->
                new NotificationResponse(
                    n.getId(),
                    n.getType().name(),
                    n.getMessage(),
                    n.isRead(),
                    n.getCreationDate().toString()))
        .toList();
  }

  @Transactional
  public void markAllRead(String userId) {
    repo.markAllReadByUserId(userId);
  }

  @Transactional
  public void markOneRead(String notifId) {
    repo.findById(notifId)
        .ifPresent(
            n -> {
              n.markRead();
              repo.save(n);
            });
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getAllNotifications() {

    return repo.findAll().stream()
        .sorted(java.util.Comparator.comparing(Notification::getCreationDate).reversed())
        .map(
            n ->
                new NotificationResponse(
                    n.getId(),
                    n.getType().name(),
                    n.getMessage(),
                    n.isRead(),
                    n.getCreationDate().toString()))
        .toList();
  }
}
