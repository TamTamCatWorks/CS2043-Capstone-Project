package org.tamtamcatworks.auction.server.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.tamtamcatworks.auction.server.service.admin.AdminService;
import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;
import org.tamtamcatworks.auction.shared.response.AdminReportResponse;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;
import org.tamtamcatworks.auction.shared.response.AdminDashboardResponse;
import org.tamtamcatworks.auction.server.service.admin.AdminAnalyticsService;
import org.tamtamcatworks.auction.server.security.admin.AdminGuard;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminAnalyticsService analyticsService;

    private final AdminService adminService;

    private final AdminGuard adminGuard;
    public AdminController(

        AdminService adminService,

        AdminAnalyticsService analyticsService,

        AdminGuard adminGuard
    ) {

        this.adminService =
            adminService;

        this.analyticsService =
            analyticsService;

        this.adminGuard = 
            adminGuard;
    }

    private UserResponse getCurrentUser() {

        return new UserResponse(

            "1",

            "admin",

            "admin@gmail.com",

            "System Admin",

            0,

            0,

            true,

            java.util.List.of(
                    "ADMIN"
            )
        );
    }
    // ── USERS ─────────────────────────────

    @GetMapping("/users")
    public List<UserResponse> getUsers() {

        adminGuard.requireAdmin(

                getCurrentUser()
        );

        return adminService.getUsers();
    }

    @PatchMapping("/users/{id}/suspend")
    public void suspendUser(
            @PathVariable String id
    ) {

        adminGuard.requireAdmin(
            getCurrentUser()
        );
    }

    @PatchMapping("/users/{id}/activate")
    public void activateUser(
            @PathVariable String id
    ) {

        adminGuard.requireAdmin(
            getCurrentUser()
        );
    }

    // ── AUCTIONS ──────────────────────────

    @GetMapping("/auctions")
    public List<AuctionResponse> getAuctions() {

        adminGuard.requireAdmin(

                getCurrentUser()
        );

        return adminService.getAuctions();
    }

    @PatchMapping("/auctions/{id}/close")
    public void closeAuction(
            @PathVariable String id
    ) {

        adminGuard.requireAdmin(
            getCurrentUser()
        );
    }

    // ── REPORTS ───────────────────────────

    @GetMapping("/reports")
    public List<AdminReportResponse> getReports() {

        adminGuard.requireAdmin(

            getCurrentUser()
        );

        return adminService.getReports();
    }

    @PatchMapping("/reports/{id}/resolve")
    public void resolveReport(
            @PathVariable String id
    ) {

        adminGuard.requireAdmin(
            getCurrentUser()
        );
    }

    @PatchMapping("/reports/{id}/reject")
    public void rejectReport(
            @PathVariable String id
    ) {

        adminGuard.requireAdmin(
            getCurrentUser()
        );
    }

    // ── AUDIT LOGS ────────────────────────

    @GetMapping("/audit-logs")
    public List<AdminAuditLogResponse>
    getAuditLogs() {

        adminGuard.requireAdmin(

            getCurrentUser()
        );

        return adminService.getAuditLogs();
    }

    // ── DASHBOARD ────────────────────────

    @GetMapping("/dashboard")
    public List<AdminDashboardResponse>
    getDashboardAnalytics() {

        adminGuard.requireAdmin(

                getCurrentUser()
        );

        return adminService.getDashboardAnalytics();
    }
}