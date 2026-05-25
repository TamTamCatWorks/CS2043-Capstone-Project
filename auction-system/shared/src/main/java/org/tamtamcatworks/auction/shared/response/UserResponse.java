package org.tamtamcatworks.auction.shared.response;

public record UserResponse(
    String id,
    String username,
    String email,
    String fullName,
    double balance,
    boolean isAdmin,
    java.util.List<String> permissions
) {
}
