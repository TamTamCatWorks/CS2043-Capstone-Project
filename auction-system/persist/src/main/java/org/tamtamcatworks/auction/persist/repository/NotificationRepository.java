package org.tamtamcatworks.auction.persist.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tamtamcatworks.auction.model.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, String> {

  List<Notification> findByUserIdOrderByCreationDateDesc(String userId);

  List<Notification> findByUserIdAndIsReadFalseOrderByCreationDateDesc(String userId);

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
  void markAllReadByUserId(String userId);
}
