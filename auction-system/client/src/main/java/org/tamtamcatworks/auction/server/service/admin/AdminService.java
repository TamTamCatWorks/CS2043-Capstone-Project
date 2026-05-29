package org.tamtamcatworks.auction.server.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.tamtamcatworks.auction.shared.response.*;
import org.tamtamcatworks.auction.server.exception.NotFoundException;
import org.tamtamcatworks.auction.server.exception.ForbiddenException;

@Service
public class AdminService {

    public List<UserResponse> getUsers() {

        return List.of();
    }

    public void suspendUser(
        String id
    ) {

        if (id == null || id.isBlank()) {

            throw new NotFoundException(

                    "User not found"
            );
        }
    }

    public void activateUser(
            String id
    ) {

    }

    public List<AuctionResponse> getAuctions() {

        return List.of();
    }

    public void closeAuction(
            String id
    ) {

    }

    public List<AdminReportResponse> getReports() {

        return List.of();
    }

    public void resolveReport(
            String id
    ) {

    }

    public void rejectReport(
            String id
    ) {

    }

    public List<AdminAuditLogResponse>
    getAuditLogs() {

        return List.of();
    }

    public List<AdminDashboardResponse> 
    getDashboardAnalytics() {
        return List.of();
    }
}