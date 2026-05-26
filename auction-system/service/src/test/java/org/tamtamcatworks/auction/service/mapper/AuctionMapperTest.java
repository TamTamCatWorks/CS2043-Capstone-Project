package org.tamtamcatworks.auction.service.mapper;

import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuctionMapperTest {

    private final AuctionMapper auctionMapper = loadMapper();

    @Test
    void toResponseIncludesAuctionStateWithoutLeadingBidder() {
        User seller = new User("seller", "seller@example.com", "hash", "Seller Name", 1000);
        Art item = new Art(
            "Painting",
            "Description",
            5000,
            ItemCondition.NEW,
            "https://example.com/item.jpg",
            seller,
            "Artist Name",
            2020,
            "Oil",
            "40x50 cm",
            true
        );
        Auction auction = new Auction(
            "Auction Title",
            seller,
            item,
            5000,
            LocalDateTime.of(2026, 5, 21, 10, 0),
            LocalDateTime.of(2026, 5, 22, 10, 0)
        );

        AuctionResponse response = auctionMapper.toResponse(auction);

        assertEquals(auction.getId(), response.id());
        assertEquals(AuctionStatus.PENDING.name(), response.status());
        assertEquals(5000, response.currentPrice());
        assertNull(response.leadingBidderId());
        assertNull(response.leadingBidderName());
    }

    @Test
    void toResponseIncludesLeadingBidderAfterBidIsRecorded() {
        User seller = new User("seller", "seller@example.com", "hash", "Seller Name", 1000);
        User bidder = new User("bidder", "bidder@example.com", "hash", "Bidder Name", 2000);
        Art item = new Art(
            "Painting",
            "Description",
            5000,
            ItemCondition.NEW,
            "https://example.com/item.jpg",
            seller,
            "Artist Name",
            2020,
            "Oil",
            "40x50 cm",
            true
        );
        Auction auction = new Auction(
            "Auction Title",
            seller,
            item,
            5000,
            LocalDateTime.of(2026, 5, 21, 10, 0),
            LocalDateTime.of(2026, 5, 22, 10, 0)
        );
        auction.open();
        auction.recordBid(new BidTransaction(auction, bidder, 7500, BidTransaction.BidType.MANUAL));

        AuctionResponse response = auctionMapper.toResponse(auction);

        assertEquals(auction.getId(), response.id());
        assertEquals(AuctionStatus.ACTIVE.name(), response.status());
        assertEquals(7500, response.currentPrice());
        assertEquals(bidder.getId(), response.leadingBidderId());
        assertEquals(bidder.getFullName(), response.leadingBidderName());
    }

    private static AuctionMapper loadMapper() {
        try {
            Class<?> mapperImpl = Class.forName("org.tamtamcatworks.auction.service.mapper.AuctionMapperImpl");
            return assertInstanceOf(AuctionMapper.class, mapperImpl.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to load AuctionMapperImpl", exception);
        }
    }
}