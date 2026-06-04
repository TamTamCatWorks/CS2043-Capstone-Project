package org.tamtamcatworks.auction.service.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.notification.Notification;
import org.tamtamcatworks.auction.model.notification.NotificationType;
import org.tamtamcatworks.auction.persist.repository.NotificationRepository;
import org.tamtamcatworks.auction.service.event.AuctionEvent;
import org.tamtamcatworks.auction.service.event.BidEvent;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository repo;

  private NotificationService service;

  @Captor private ArgumentCaptor<Notification> notificationCaptor;

  @BeforeEach
  void setUp() {
    service = new NotificationService(repo);
  }

  @Test
  void testOnBidPlaced() {
    BidEvent event =
        new BidEvent("auc123", "Comic Sale", "seller123", "bidder123", "prev123", 150.0);

    service.onBidPlaced(event);

    // Should save two notifications: one to seller, one to previous leader
    verify(repo, times(2)).save(notificationCaptor.capture());

    List<Notification> captured = notificationCaptor.getAllValues();
    assertEquals("seller123", captured.get(0).getUserId());
    assertEquals(NotificationType.BID_PLACED, captured.get(0).getType());

    assertEquals("prev123", captured.get(1).getUserId());
    assertEquals(NotificationType.OUTBID, captured.get(1).getType());
  }

  @Test
  void testOnAuctionStatusChangedActive() {
    AuctionEvent event =
        new AuctionEvent(
            "auc123", "Comic Sale", "seller123", null, 100.0, AuctionStatus.ACTIVE, null);

    service.onAuctionStatusChanged(event);

    verify(repo, times(1)).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals("seller123", notification.getUserId());
    assertEquals(NotificationType.AUCTION_OPENED, notification.getType());
    assertTrue(notification.getMessage().contains("live"));
  }

  @Test
  void testOnAuctionStatusChangedClosed() {
    AuctionEvent event =
        new AuctionEvent(
            "auc123", "Comic Sale", "seller123", "winner123", 500.0, AuctionStatus.CLOSED, null);

    service.onAuctionStatusChanged(event);

    // Saves two notifications: one to seller, one to winner
    verify(repo, times(2)).save(notificationCaptor.capture());
    List<Notification> captured = notificationCaptor.getAllValues();

    assertEquals("seller123", captured.get(0).getUserId());
    assertEquals(NotificationType.AUCTION_CLOSED, captured.get(0).getType());

    assertEquals("winner123", captured.get(1).getUserId());
    assertEquals(NotificationType.AUCTION_CLOSED, captured.get(1).getType());
    assertTrue(captured.get(1).getMessage().contains("won"));
  }

  @Test
  void testMarkOneRead() {
    Notification notification = new Notification("user123", NotificationType.OUTBID, "Outbid!");
    assertFalse(notification.isRead());
    when(repo.findById("notif123")).thenReturn(Optional.of(notification));

    service.markOneRead("notif123");

    assertTrue(notification.isRead());
    verify(repo, times(1)).save(notification);
  }
}
