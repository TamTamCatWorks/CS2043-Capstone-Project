package org.tamtamcatworks.auction.shared.response;

public record AdminDashboardResponse(

        long totalUsers,

        long activeAuctions,

        long pendingReports,

        double totalRevenue
) {
}