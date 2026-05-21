package org.tamtamcatworks.auction.service.mapper;

import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.response.BidResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BidMapperTest {

    private final BidMapper bidMapper = loadMapper();

    @Test
    void toResponseMapsBidTransactionToSharedDto() {
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
        BidTransaction bidTransaction = new BidTransaction(auction, bidder, 7500, BidTransaction.BidType.MANUAL);

        BidResponse response = bidMapper.toResponse(bidTransaction);

        assertEquals(bidTransaction.getId(), response.id());
        assertEquals(auction.getId(), response.auctionId());
        assertEquals(bidder.getId(), response.bidderId());
        assertEquals(bidder.getFullName(), response.bidderName());
        assertEquals(7500, response.amount());
        assertEquals(BidTransaction.BidType.MANUAL.name(), response.bidType());
        assertEquals(bidTransaction.getCreationDate(), response.createdAt());
    }

    private static BidMapper loadMapper() {
        try {
            Class<?> mapperImpl = Class.forName("org.tamtamcatworks.auction.service.mapper.BidMapperImpl");
            return assertInstanceOf(BidMapper.class, mapperImpl.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to load BidMapperImpl", exception);
        }
    }
}