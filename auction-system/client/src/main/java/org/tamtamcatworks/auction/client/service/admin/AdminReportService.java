package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;

import org.tamtamcatworks.auction.shared.response.AdminReportResponse;

public class AdminReportService
        extends BaseAdminService {

    public List<AdminReportResponse>
    getReports() {

        return java.util.List.of(

                new AdminReportResponse(
                        "1",
                        "Auction",
                        "iPhone 15 Auction",
                        "Fake product",
                        "PENDING"
                ),

                new AdminReportResponse(
                        "2",
                        "User",
                        "john_doe",
                        "Spam bidding",
                        "PENDING"
                )
        );
    }

    public void resolveReport(
            String reportId
    ) {

        System.out.println(
                "Resolved report: " + reportId
        );
    }

    public void rejectReport(
            String reportId
    ) {

        System.out.println(
                "Rejected report: " + reportId
        );
    }
}