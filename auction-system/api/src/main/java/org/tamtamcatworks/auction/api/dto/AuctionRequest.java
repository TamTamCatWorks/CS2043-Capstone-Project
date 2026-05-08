package org.tamtamcatworks.auction.api.dto;

import java.time.LocalDateTime;

public record AuctionRequest(String title, String itemId,
                             double startingPrice, LocalDateTime startTime, LocalDateTime endTime) {}
