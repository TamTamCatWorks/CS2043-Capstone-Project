package org.tamtamcatworks.auction.model.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for Notification and NotificationType. */
public class NotificationTest {

  @Test
  public void testNotification() {
    Notification notif =
        new Notification("user-1", NotificationType.OUTBID, "You have been outbid!");

    assertEquals("user-1", notif.getUserId());
    assertEquals(NotificationType.OUTBID, notif.getType());
    assertEquals("You have been outbid!", notif.getMessage());
    assertFalse(notif.isRead());

    notif.markRead();
    assertTrue(notif.isRead());
  }

  @Test
  public void testNotificationTypes() {
    assertEquals(5, NotificationType.values().length);
    assertEquals(NotificationType.OUTBID, NotificationType.valueOf("OUTBID"));
  }
}
