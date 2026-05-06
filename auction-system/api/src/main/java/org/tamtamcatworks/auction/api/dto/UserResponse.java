package org.tamtamcatworks.auction.api.dto;

import org.tamtamcatworks.auction.model.user.User;

public record UserResponse(String id, String username, String email, String fullName, double balance) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getBalance()
        );
    }
}