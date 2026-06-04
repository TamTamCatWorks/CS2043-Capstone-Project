package org.tamtamcatworks.auction.api.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tamtamcatworks.auction.service.notification.NotificationService;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getMyNotifications(HttpSession session) {
    String userId = (String) session.getAttribute("userId");
    return ResponseEntity.ok(notificationService.getForUser(userId));
  }

  @PatchMapping("/read-all")
  public ResponseEntity<Void> markAllRead(HttpSession session) {
    String userId = (String) session.getAttribute("userId");
    notificationService.markAllRead(userId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<Void> markOneRead(@PathVariable String id) {
    notificationService.markOneRead(id);
    return ResponseEntity.noContent().build();
  }
}
