package org.tamtamcatworks.auction.shared.response;

public record UserResponse(
    String id,
    String username,
    String email,
    String fullName,
    double balance,
    double holdBalance,
    boolean isActive,
    boolean isAdmin,
    java.util.List<String> permissions
) {
}
