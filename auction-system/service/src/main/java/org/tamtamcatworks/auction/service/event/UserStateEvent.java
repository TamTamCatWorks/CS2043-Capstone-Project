package org.tamtamcatworks.auction.service.event;

import org.tamtamcatworks.auction.shared.response.UserResponse;

public record UserStateEvent(String userId, UserResponse user) {
}