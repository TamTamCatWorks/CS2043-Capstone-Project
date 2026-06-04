package org.tamtamcatworks.auction.shared.response;

public record AdminReportResponse(
    String id, String targetType, String targetName, String reason, String status) {}
