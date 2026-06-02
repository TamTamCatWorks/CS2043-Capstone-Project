package org.tamtamcatworks.auction.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tamtamcatworks.auction.service.member.UserService;
import org.tamtamcatworks.auction.service.notification.NotificationService;
import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;
import org.tamtamcatworks.auction.shared.response.AdminDashboardResponse;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;
import org.tamtamcatworks.auction.service.auction.AuctionService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final AuctionService auctionService;

    public AdminController(UserService userService, NotificationService notificationService, AuctionService auctionService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.auctionService = auctionService;
    }

    @PostMapping("/promote/{userId}")
    public ResponseEntity<UserResponse> promoteUser(
            @PathVariable String userId,
            @RequestBody List<String> permissions) {
        return ResponseEntity.ok(userService.promoteToAdmin(userId, permissions));
    }

    @GetMapping("/logs/{userId}")
    public ResponseEntity<List<String>> getActionLogs(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getAdminActionLogs(userId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {

        return ResponseEntity.ok(

            userService.getAllUsers()
        );
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable String id) {

        userService.suspendUser(id);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable String id) {

        userService.activateUser(id);

        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {

        return ResponseEntity.ok(

            userService.getDashboardStats()
        );
    }

    @GetMapping("/logs")
    public ResponseEntity<List<String>> getLogs() {

        return ResponseEntity.ok(
            userService.getAllAdminLogs()
        );
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AdminAuditLogResponse>> getAuditLogs() {

        return ResponseEntity.ok(
            userService.getAuditLogs()
        );
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {

        return ResponseEntity.ok(
                notificationService.getAllNotifications()
        );
    }

    @PatchMapping("/auctions/{id}/close")
    public ResponseEntity<AuctionResponse> closeAuction(@PathVariable String id) {

        return ResponseEntity.ok(auctionService.closeById(id));
    }

    @PatchMapping("/auctions/{id}/open")
    public ResponseEntity<AuctionResponse> openAuction(@PathVariable String id) {

        return ResponseEntity.ok(auctionService.openById(id));
    }

    @GetMapping("/auctions")
    public ResponseEntity<List<AuctionResponse>> getAllAuctions() {

        return ResponseEntity.ok(auctionService.getAllAuctions());
    }
}