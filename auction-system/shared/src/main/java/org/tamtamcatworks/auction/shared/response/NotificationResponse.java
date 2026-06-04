package org.tamtamcatworks.auction.shared.response;

/** DTO for a single notification returned to the client. */
public record NotificationResponse(
    String id, String type, String message, boolean read, String createdAt) {}
