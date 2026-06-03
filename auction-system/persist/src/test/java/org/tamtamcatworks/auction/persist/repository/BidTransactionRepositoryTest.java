package org.tamtamcatworks.auction.persist.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.Other;
import org.tamtamcatworks.auction.model.user.User;

@DataJpaTest
class BidTransactionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidTransactionRepository bidTransactionRepository;

    private User seller;
    private User bidder;
    private Auction auction;
    private BidTransaction tx1;
    private BidTransaction tx2;

    @BeforeEach
    void setUp() {
        seller = new User("seller", "seller@example.com", "pw", "John Seller", 1000.0);
        bidder = new User("bidder", "bidder@example.com", "pw", "Bob Bidder", 2000.0);
        userRepository.save(seller);
        userRepository.save(bidder);

        Other item = new Other("Collectible", "Rare comic", 10.0, ItemCondition.POOR, "img", seller);
        itemRepository.save(item);

        auction = new Auction("Comic Auction", seller, item, 10.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        auction.open();
        auctionRepository.save(auction);

        tx1 = new BidTransaction(auction, bidder, 12.0, BidTransaction.BidType.MANUAL);
        tx2 = new BidTransaction(auction, bidder, 15.0, BidTransaction.BidType.MANUAL);

        bidTransactionRepository.save(tx1);
        bidTransactionRepository.save(tx2);
    }

    @Test
    void testFindByAuctionOrderByCreationDateAsc() {
        List<BidTransaction> bids = bidTransactionRepository.findByAuctionOrderByCreationDateAsc(auction);
        assertEquals(2, bids.size());
        assertEquals(12.0, bids.get(0).getAmount());
        assertEquals(15.0, bids.get(1).getAmount());
    }

    @Test
    void testFindByBidderOrderByCreationDateAsc() {
        List<BidTransaction> bids = bidTransactionRepository.findByBidderOrderByCreationDateAsc(bidder);
        assertEquals(2, bids.size());
        assertEquals(12.0, bids.get(0).getAmount());
        assertEquals(15.0, bids.get(1).getAmount());
    }
}
