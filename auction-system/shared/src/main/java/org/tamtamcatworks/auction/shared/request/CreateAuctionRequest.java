package org.tamtamcatworks.auction.shared.request;

import java.time.LocalDateTime;

public record CreateAuctionRequest(String title, ItemRequest item,
                                   LocalDateTime startTime, LocalDateTime endTime) {
}
