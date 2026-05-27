package org.tamtamcatworks.auction.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tamtamcatworks.auction.service.member.UserService;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
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
}