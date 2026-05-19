package org.tamtamcatworks.auction.shared.request;

public record RegisterRequest(String username, String email, String password, String fullName) {
}
