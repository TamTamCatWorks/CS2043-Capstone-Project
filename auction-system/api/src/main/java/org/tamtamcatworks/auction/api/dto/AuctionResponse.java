package org.tamtamcatworks.auction.api.dto;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.user.User;

import java.time.LocalDateTime;

public record AuctionResponse(String title, User seller, Item item,
                              double startingPrice,
                              LocalDateTime startTime, LocalDateTime endTime) {
    public static AuctionResponse from (Auction auction) {
        return new AuctionResponse(
                auction.getTitle(),
                auction.getSeller(),
                auction.getItem(),
                auction.getStartingPrice(),
                auction.getStartTime(),
                auction.getEndTime()
        );
    }
}