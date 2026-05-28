package org.tamtamcatworks.auction.client.service.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;

public class AdminAuditLogService
        extends BaseAdminService {

    public List<AdminAuditLogResponse>
    getAuditLogs() {

        return List.of(

                new AdminAuditLogResponse(

                        "1",

                        "super_admin",

                        "Suspended User",

                        "john_doe",

                        LocalDateTime.now()
                                .minusHours(2)
                ),

                new AdminAuditLogResponse(

                        "2",

                        "moderator",

                        "Closed Auction",

                        "iPhone Auction",

                        LocalDateTime.now()
                                .minusMinutes(30)
                )
        );
    }
}