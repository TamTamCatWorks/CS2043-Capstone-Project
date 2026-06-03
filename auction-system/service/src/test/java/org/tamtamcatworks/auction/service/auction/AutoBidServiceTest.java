package org.tamtamcatworks.auction.service.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AutoBid;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.Other;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.AutoBidRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.event.BidEvent;
import org.tamtamcatworks.auction.shared.request.AutoBidRequest;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.AutoBidResponse;

@ExtendWith(MockitoExtension.class)
class AutoBidServiceTest {

    @Mock
    private AutoBidRepository autoBidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BidService bidService;

    private AutoBidService autoBidService;

    private User seller;
    private User bidder1;
    private User bidder2;
    private Auction auction;

    @BeforeEach
    void setUp() {
        autoBidService = new AutoBidService(autoBidRepository, auctionRepository, userRepository, bidService);
        ReflectionTestUtils.setField(autoBidService, "self", autoBidService);

        seller = new User("seller", "seller@example.com", "pw", "John Seller", 100.0);
        ReflectionTestUtils.setField(seller, "id", "sellerId");

        bidder1 = new User("bidder1", "bidder1@example.com", "pw", "Bob Bidder 1", 1000.0);
        ReflectionTestUtils.setField(bidder1, "id", "bidder1Id");

        bidder2 = new User("bidder2", "bidder2@example.com", "pw", "Bob Bidder 2", 1000.0);
        ReflectionTestUtils.setField(bidder2, "id", "bidder2Id");

        Other item = new Other("Comic", "Rare book", 100.0, ItemCondition.GOOD, "img", seller);
        auction = new Auction("Comic Sale", seller, item, 100.0, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(auction, "id", "auctionId");
        ReflectionTestUtils.setField(auction, "minimumIncrement", 10.0);
        auction.open();
    }

    @Test
    void testRegisterSuccess() {
        AutoBidRequest req = new AutoBidRequest(200.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.empty());
        when(autoBidRepository.save(any(AutoBid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AutoBidResponse resp = autoBidService.register("auctionId", "bidder1Id", req);

        assertNotNull(resp);
        assertEquals(200.0, resp.maxBid());
        assertEquals("bidder1Id", resp.bidderId());
        assertTrue(resp.active());
    }

    @Test
    void testRegisterSellerThrowsException() {
        AutoBidRequest req = new AutoBidRequest(200.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("sellerId")).thenReturn(Optional.of(seller));

        assertThrows(IllegalStateException.class, () -> 
            autoBidService.register("auctionId", "sellerId", req)
        );
    }

    @Test
    void testCancelSuccess() {
        AutoBid ab = new AutoBid(auction, bidder1, 200.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.of(ab));

        autoBidService.cancel("auctionId", "bidder1Id");

        verify(autoBidRepository, times(1)).save(ab);
        assertTrue(!ab.isActive());
    }

    @Test
    void testOnBidPlacedPriorityQueueOrdering() {
        // Setup two auto-bidders
        AutoBid ab1 = new AutoBid(auction, bidder1, 150.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        // creationDate is set via @PrePersist on save. Let's set it manually using reflection to simulate different registration times.
        LocalDateTime earlyTime = LocalDateTime.now().minusMinutes(10);
        ReflectionTestUtils.setField(ab1, "creationDate", earlyTime);

        AutoBid ab2 = new AutoBid(auction, bidder2, 150.0);
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        LocalDateTime lateTime = LocalDateTime.now().minusMinutes(5);
        ReflectionTestUtils.setField(ab2, "creationDate", lateTime);

        List<AutoBid> candidates = Arrays.asList(ab2, ab1); // Bidding list from repo: later registration first

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(candidates);

        // Current leader is a manual bidder (not bidder1 or bidder2)
        User manualBidder = new User("manual", "m@example.com", "pw", "Manual Bidder", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        BidEvent event = new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0);

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(event);

        // We want to verify that `executeAutoBid` was called with the best candidate.
        // Because both have maxBid = 150.0, the one with the earlier creationDate (ab1) must be prioritized.
        ArgumentCaptor<String> autoBidIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bidderIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);

        verify(spyService).executeAutoBid(eq("auctionId"), autoBidIdCaptor.capture(), bidderIdCaptor.capture(), amountCaptor.capture());

        assertEquals("ab1Id", autoBidIdCaptor.getValue()); // ab1 had the earlier creationDate
        assertEquals("bidder1Id", bidderIdCaptor.getValue());
        assertEquals(110.0, amountCaptor.getValue()); // 100.0 currentPrice + 10.0 minimumIncrement
    }

    @Test
    void testExecuteAutoBidFailureDeactivates() {
        // Mock proxy for deactivateAutoBid
        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        // Simulate bidService.placeBid throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(bidService).placeBid(eq("auctionId"), eq("bidder1Id"), any(BidRequest.class));

        autoBidService.executeAutoBid("auctionId", "ab1Id", "bidder1Id", 120.0);

        // Should call self.deactivateAutoBid(ab1Id)
        verify(spyService, times(1)).deactivateAutoBid("ab1Id");
    }
}
