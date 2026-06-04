package org.tamtamcatworks.auction.persist.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.tamtamcatworks.auction.model.notification.Notification;
import org.tamtamcatworks.auction.model.notification.NotificationType;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private String userId = "user123";
    private Notification n1;
    private Notification n2;

    @BeforeEach
    void setUp() {
        n1 = new Notification(userId, NotificationType.BID_PLACED, "Someone bid on your item.");
        n2 = new Notification(userId, NotificationType.OUTBID, "You have been outbid.");
        notificationRepository.save(n1);
        notificationRepository.save(n2);
    }

    @Test
    void testFindByUserIdOrderByCreationDateDesc() {
        List<Notification> all = notificationRepository.findByUserIdOrderByCreationDateDesc(userId);
        assertEquals(2, all.size());
    }

    @Test
    void testFindByUserIdAndIsReadFalseOrderByCreationDateDesc() {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreationDateDesc(userId);
        assertEquals(2, unread.size());

        n1.markRead();
        notificationRepository.save(n1);

        unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreationDateDesc(userId);
        assertEquals(1, unread.size());
        assertEquals("You have been outbid.", unread.get(0).getMessage());
    }

    @Test
    void testMarkAllReadByUserId() {
        notificationRepository.markAllReadByUserId(userId);
        
        // DataJpaTest runs with an EntityManager cache, so we should clear it or verify 
        // that querying unread notifications yields zero unread notifications.
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreationDateDesc(userId);
        assertTrue(unread.isEmpty());
    }
}
