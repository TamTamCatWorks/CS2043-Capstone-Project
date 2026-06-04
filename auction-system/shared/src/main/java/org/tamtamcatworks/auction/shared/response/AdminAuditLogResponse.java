package org.tamtamcatworks.auction.shared.response;

import java.time.LocalDateTime;

public record AdminAuditLogResponse(
    String id, String adminName, String action, String target, LocalDateTime timestamp) {}
