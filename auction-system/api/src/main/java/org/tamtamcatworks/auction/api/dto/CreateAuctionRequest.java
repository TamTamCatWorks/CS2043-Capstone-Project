package org.tamtamcatworks.auction.api.dto;

import org.tamtamcatworks.auction.model.item.Item;

import java.time.LocalDateTime;

public record CreateAuctionRequest(String title, Item item,
                                   LocalDateTime startTime, LocalDateTime endTime) {
}
