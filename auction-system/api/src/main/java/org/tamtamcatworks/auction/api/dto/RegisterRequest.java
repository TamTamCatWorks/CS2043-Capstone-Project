package org.tamtamcatworks.auction.api.dto;

public record RegisterRequest(String username, String email, String password, String fullName) {}