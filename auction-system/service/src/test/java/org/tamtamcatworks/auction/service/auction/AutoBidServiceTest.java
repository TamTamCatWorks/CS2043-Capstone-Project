package org.tamtamcatworks.auction.service.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.springframework.context.ApplicationEventPublisher;
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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AutoBidService autoBidService;

    private User seller;
    private User bidder1;
    private User bidder2;
    private Auction auction;

    @BeforeEach
    void setUp() {
        autoBidService = new AutoBidService(autoBidRepository, auctionRepository, userRepository, bidService, eventPublisher);
        ReflectionTestUtils.setField(autoBidService, "self", autoBidService);

        seller = new User("seller", "seller@example.com", "pw", "John Seller", 1000.0);
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
        // Two-way tie: tied group resolved in one pass.
        // ab1 wins at minimumNext (100 + 10 = 110), NOT the shared ceiling (150).
        // ab2 (the other tied member) is deactivated in the same invocation.
        assertEquals(110.0, amountCaptor.getValue());
        verify(spyService).deactivateAutoBid("ab2Id");
    }

    @Test
    void testExecuteAutoBidFailureDeactivates() {
        // Mock proxy for deactivateAutoBid
        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        // Stub repository lookups so pre-validation in executeAutoBid passes through
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        // bidder1 balance=1000.0 >= 120.0, so balance check passes

        // Simulate bidService.placeBid throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(bidService).placeBid(eq("auctionId"), eq("bidder1Id"), any(BidRequest.class));

        autoBidService.executeAutoBid("auctionId", "ab1Id", "bidder1Id", 120.0);

        // Should call self.deactivateAutoBid(ab1Id)
        verify(spyService, times(1)).deactivateAutoBid("ab1Id");
    }

    // ── onBidPlaced resolution cases ─────────────────────────────────────────

    @Test
    void testOnBidPlacedCase1_SoleAutoBidder_PaysMinimumIncrement() {
        // Case 1: only one auto-bidder active, no competitor.
        // Expected: winningPrice = currentPrice + minimumIncrement = 100 + 10 = 110
        AutoBid ab1 = new AutoBid(auction, bidder1, 300.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(List.of(ab1));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0));

        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(spyService).executeAutoBid(eq("auctionId"), eq("ab1Id"), eq("bidder1Id"), amountCaptor.capture());
        assertEquals(110.0, amountCaptor.getValue());
    }

    @Test
    void testOnBidPlacedCase2_TwoAutosBidders_WinnerPaysJustEnough() {
        // Case 2: best.maxBid=300, second.maxBid=200, increment=10
        // winningPrice = min(300, 200+10) = 210
        AutoBid ab1 = new AutoBid(auction, bidder1, 300.0); // best
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab2 = new AutoBid(auction, bidder2, 200.0); // second
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(5));

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0));

        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(spyService).executeAutoBid(eq("auctionId"), eq("ab1Id"), eq("bidder1Id"), amountCaptor.capture());
        assertEquals(210.0, amountCaptor.getValue());
    }

    @Test
    void testOnBidPlacedCase2_WinnerCappedAtOwnMaxBid() {
        // Case 2: best.maxBid=205, second.maxBid=200, increment=10
        // second.maxBid + increment = 210 > best.maxBid = 205
        // winningPrice = min(205, 210) = 205
        AutoBid ab1 = new AutoBid(auction, bidder1, 205.0); // best
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab2 = new AutoBid(auction, bidder2, 200.0); // second
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(5));

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0));

        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(spyService).executeAutoBid(eq("auctionId"), eq("ab1Id"), eq("bidder1Id"), amountCaptor.capture());
        assertEquals(205.0, amountCaptor.getValue());
    }

    @Test
    void testOnBidPlacedCase3_BestBelowMinimum_Deactivates() {
        // Case 3: best.maxBid < currentPrice + increment → deactivate, no bid placed.
        // currentPrice=100, increment=10, minimumNext=110, best.maxBid=105
        AutoBid ab1 = new AutoBid(auction, bidder1, 105.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(List.of(ab1));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0));

        verify(spyService, times(1)).deactivateAutoBid("ab1Id");
        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));
    }

    @Test
    void testOnBidPlacedNWayTie_AllPeersDeactivated_EarliestWinsAtMinimumNext() {
        // Three-way tie: ab1 (200, earliest), ab2 (200, mid), ab3 (200, latest).
        // Expected: ab1 wins at currentPrice + increment = 110 (NOT the shared ceiling 200).
        // ab2 and ab3 are deactivated in the same invocation — no cascade.
        AutoBid ab1 = new AutoBid(auction, bidder1, 200.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(20));

        AutoBid ab2 = new AutoBid(auction, bidder2, 200.0);
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(10));

        User bidder3 = new User("bidder3", "bidder3@example.com", "pw", "Bob Bidder 3", 1000.0);
        ReflectionTestUtils.setField(bidder3, "id", "bidder3Id");
        AutoBid ab3 = new AutoBid(auction, bidder3, 200.0);
        ReflectionTestUtils.setField(ab3, "id", "ab3Id");
        ReflectionTestUtils.setField(ab3, "creationDate", LocalDateTime.now().minusMinutes(5));

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2, ab3));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0));

        // ab1 (earliest) wins at minimumNext = 110.
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(spyService).executeAutoBid(eq("auctionId"), eq("ab1Id"), eq("bidder1Id"), amountCaptor.capture());
        assertEquals(110.0, amountCaptor.getValue());

        // ab2 and ab3 are deactivated immediately in the same pass.
        verify(spyService, times(1)).deactivateAutoBid("ab2Id");
        verify(spyService, times(1)).deactivateAutoBid("ab3Id");
    }

    @Test
    void testOnBidPlacedNWayTie_WithOutsider_WinnerPriceFromOutsider() {
        // Two tied at the top (ab1, ab2 both 200) plus a non-tied outsider (ab3 at 150).
        // ab2 is deactivated as a tied peer; ab3 becomes the effective "second".
        // winningPrice = min(200, 150 + 10) = 160 — set by the outsider, not the tied peer.
        AutoBid ab1 = new AutoBid(auction, bidder1, 200.0); // tied best, earlier
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(20));

        AutoBid ab2 = new AutoBid(auction, bidder2, 200.0); // tied peer — deactivated
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(10));

        User bidder3 = new User("bidder3", "bidder3@example.com", "pw", "Bob Bidder 3", 1000.0);
        ReflectionTestUtils.setField(bidder3, "id", "bidder3Id");
        AutoBid ab3 = new AutoBid(auction, bidder3, 150.0); // non-tied outsider (second)
        ReflectionTestUtils.setField(ab3, "id", "ab3Id");
        ReflectionTestUtils.setField(ab3, "creationDate", LocalDateTime.now().minusMinutes(5));

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2, ab3));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0));

        // ab1 wins; price is set by the outsider (ab3), not the tied peer (ab2).
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(spyService).executeAutoBid(eq("auctionId"), eq("ab1Id"), eq("bidder1Id"), amountCaptor.capture());
        assertEquals(160.0, amountCaptor.getValue());

        // ab2 (tied peer) is deactivated; ab3 (outsider) is untouched.
        verify(spyService, times(1)).deactivateAutoBid("ab2Id");
        verify(spyService, never()).deactivateAutoBid("ab3Id");
    }

    @Test
    void testOnBidPlacedCase3_WithSecondBidder_OnlyBestDeactivatedThisPass() {
        // Edge case 4 — Case 3 with a second auto-bidder present.
        // Both are below minimumNext (110): ab1.maxBid=109 (best), ab2.maxBid=105 (second).
        // Only the best (ab1) is deactivated this pass; ab2 is intentionally left active
        // so it gets a chance to compete as "best" when the next BidEvent fires.
        // executeAutoBid must never be called in this invocation.
        AutoBid ab1 = new AutoBid(auction, bidder1, 109.0); // best — higher maxBid, earlier
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab2 = new AutoBid(auction, bidder2, 105.0); // second — lower maxBid, later
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(5));

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 100.0));

        // Only best (ab1) is deactivated this pass.
        verify(spyService, times(1)).deactivateAutoBid("ab1Id");
        // Second (ab2) is untouched — it waits for the next BidEvent.
        verify(spyService, never()).deactivateAutoBid("ab2Id");
        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));
    }

    @Test
    void testOnBidPlacedCascadeTerminates_SecondPassNoFurtherBid() {
        // Edge case 5 — proves the cascade dies after at most 2 onBidPlaced invocations.
        //
        // First pass (external, not modelled here):
        //   ab1 (best=300) beats ab2 (second=200) → single-jump win at min(300, 200+10)=210.
        //   That bid commits and fires a second BidEvent with bidderId="bidder1Id".
        //
        // Second pass (this test):
        //   ab1 is now the leading bidder → excluded from the queue.
        //   ab2 becomes "best" with maxBid=200; minimumNext = 210 + 10 = 220 > 200.
        //   → Case 3: ab2 deactivated, NO bid placed → cascade ends here.
        AutoBid ab1 = new AutoBid(auction, bidder1, 300.0); // prior winner, now leader
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab2 = new AutoBid(auction, bidder2, 200.0); // runner-up from first pass
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(5));

        // State after the first auto-bid committed: bidder1 leads at 210.
        ReflectionTestUtils.setField(auction, "leadingBidder", bidder1);
        ReflectionTestUtils.setField(auction, "currentPrice", 210.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        // Second BidEvent is published by bidder1's auto-bid commit.
        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "bidder1Id", "manualId", 210.0));

        // ab2 cannot meet minimumNext (220) → deactivated; no new auto-bid fires.
        verify(spyService, times(1)).deactivateAutoBid("ab2Id");
        verify(spyService, never()).deactivateAutoBid("ab1Id"); // winner is untouched
        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));
    }

    // ── register() validation tests ──────────────────────────────────────────

    @Test
    void testRegisterSuspendedBidderThrowsException() {
        bidder1.setActive(false);
        AutoBidRequest req = new AutoBidRequest(200.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            autoBidService.register("auctionId", "bidder1Id", req)
        );
        assertEquals("Suspended users cannot register auto-bids.", ex.getMessage());
        verify(autoBidRepository, never()).save(any());
    }

    @Test
    void testRegisterSuspendedSellerThrowsException() {
        seller.setActive(false);
        AutoBidRequest req = new AutoBidRequest(200.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            autoBidService.register("auctionId", "bidder1Id", req)
        );
        assertEquals("This auction belongs to a suspended seller.", ex.getMessage());
        verify(autoBidRepository, never()).save(any());
    }

    @Test
    void testRegisterInsufficientBalanceThrowsException() {
        // bidder1 has 1000.0 balance; requesting maxBid of 1500.0
        AutoBidRequest req = new AutoBidRequest(1500.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            autoBidService.register("auctionId", "bidder1Id", req)
        );
        assertEquals("Insufficient balance to cover declared maxBid.", ex.getMessage());
        verify(autoBidRepository, never()).save(any());
    }

    @Test
    void testRegisterValidEligibilitySucceeds() {
        // bidder1 balance = 1000.0, maxBid = 200.0 — should pass all checks
        AutoBidRequest req = new AutoBidRequest(200.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.empty());
        when(autoBidRepository.save(any(AutoBid.class))).thenAnswer(inv -> inv.getArgument(0));

        AutoBidResponse resp = autoBidService.register("auctionId", "bidder1Id", req);

        assertNotNull(resp);
        assertEquals(200.0, resp.maxBid());
        assertEquals("bidder1Id", resp.bidderId());
        assertTrue(resp.active());
    }

    // ── register() immediate-bid dispatch tests ────────────────────────────────────

    @Test
    void testRegisterNew_NonLeader_ImmediateBidFired() {
        // Fresh registration while NOT the leader: executeAutoBid must fire at
        // currentPrice + minimumIncrement (110), NOT at the declared maxBid (300).
        AutoBidRequest req = new AutoBidRequest(300.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.empty());
        when(autoBidRepository.save(any(AutoBid.class))).thenAnswer(inv -> {
            AutoBid ab = inv.getArgument(0);
            ReflectionTestUtils.setField(ab, "id", "ab1Id");
            return ab;
        });

        // leadingBidder = manualBidder, so bidder1 is not the leader.
        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.register("auctionId", "bidder1Id", req);

        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(spyService).executeAutoBid(eq("auctionId"), eq("ab1Id"), eq("bidder1Id"), amountCaptor.capture());
        assertEquals(110.0, amountCaptor.getValue());
    }

    @Test
    void testRegisterNew_AlreadyLeader_NoBidFired() {
        // Fresh registration where the bidder is already the leader
        // (e.g., they just placed a manual bid): no immediate bid, no synthetic event.
        AutoBidRequest req = new AutoBidRequest(300.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.empty());
        when(autoBidRepository.save(any(AutoBid.class))).thenAnswer(inv -> inv.getArgument(0));

        // bidder1 IS the current leader.
        ReflectionTestUtils.setField(auction, "leadingBidder", bidder1);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.register("auctionId", "bidder1Id", req);

        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testRegisterUpdate_NonLeader_ImmediateBidFired() {
        // Bidder raises maxBid while NOT the leader: same trigger as a fresh
        // registration — bid immediately at minimumNext (110).
        AutoBid existing = new AutoBid(auction, bidder1, 150.0);
        ReflectionTestUtils.setField(existing, "id", "ab1Id");

        AutoBidRequest req = new AutoBidRequest(300.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.of(existing));
        when(autoBidRepository.save(any(AutoBid.class))).thenReturn(existing);

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.register("auctionId", "bidder1Id", req);

        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        verify(spyService).executeAutoBid(eq("auctionId"), eq("ab1Id"), eq("bidder1Id"), amountCaptor.capture());
        assertEquals(110.0, amountCaptor.getValue());
    }

    @Test
    void testRegisterUpdate_Leader_SyntheticEventPublished() {
        // Leader raises maxBid: no immediate bid needed for themselves.
        // A synthetic BidEvent is published so onBidPlaced re-evaluates competitors
        // that may have been capped out against the old ceiling.
        AutoBid existing = new AutoBid(auction, bidder1, 250.0);
        ReflectionTestUtils.setField(existing, "id", "ab1Id");

        AutoBidRequest req = new AutoBidRequest(400.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.of(existing));
        when(autoBidRepository.save(any(AutoBid.class))).thenReturn(existing);

        // bidder1 IS the current leader at price 210.
        ReflectionTestUtils.setField(auction, "leadingBidder", bidder1);
        ReflectionTestUtils.setField(auction, "currentPrice", 210.0);

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.register("auctionId", "bidder1Id", req);

        // No direct bid for the leader.
        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));

        // Synthetic BidEvent must carry the current auction state.
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        BidEvent synthetic = (BidEvent) eventCaptor.getValue();
        assertEquals("auctionId", synthetic.auctionId());
        assertEquals("bidder1Id", synthetic.bidderId());
        assertEquals(210.0, synthetic.amount());
    }

    @Test
    void testRegisterNew_NonLeader_MaxBidBelowMinimumNext_NoBidFired() {
        // maxBid=105 passes the floor check (> currentPrice=100) but is below
        // minimumNext=110: the bidder cannot place the first bid, so no executeAutoBid.
        AutoBidRequest req = new AutoBidRequest(105.0);
        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(userRepository.findById("bidder1Id")).thenReturn(Optional.of(bidder1));
        when(autoBidRepository.findByAuctionAndBidder(auction, bidder1)).thenReturn(Optional.empty());
        when(autoBidRepository.save(any(AutoBid.class))).thenAnswer(inv -> inv.getArgument(0));

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);
        ReflectionTestUtils.setField(auction, "currentPrice", 100.0);

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.register("auctionId", "bidder1Id", req);

        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ── New cascade-termination tests (A–E) ─────────────────────────────────

    @Test
    void testOnBidPlaced_TestA_WinnerNotLeader_SinglePassResolution() {
        // Test A: two auto-bidders, winner is NOT the current leader.
        // ab1.maxBid=60000 (bidder1, current leader via manual bid),
        // ab2.maxBid=70000 (bidder2, challenger).
        // currentPrice=51000, increment=1000.
        // Expected: executeAutoBid called once for bidder2 at
        //   winningPrice = min(70000, 60000+1000) = 61000.
        ReflectionTestUtils.setField(auction, "minimumIncrement", 1000.0);
        ReflectionTestUtils.setField(auction, "currentPrice", 51000.0);
        ReflectionTestUtils.setField(auction, "leadingBidder", bidder1);

        AutoBid ab1 = new AutoBid(auction, bidder1, 60000.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab2 = new AutoBid(auction, bidder2, 70000.0);
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(5));

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "bidder1Id", null, 51000.0));

        ArgumentCaptor<String> autoBidIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bidderIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);

        verify(spyService, times(1)).executeAutoBid(
                eq("auctionId"), autoBidIdCaptor.capture(), bidderIdCaptor.capture(), amountCaptor.capture());
        assertEquals("ab2Id", autoBidIdCaptor.getValue());
        assertEquals("bidder2Id", bidderIdCaptor.getValue());
        assertEquals(61000.0, amountCaptor.getValue());
        verify(spyService, never()).deactivateAutoBid(any());
    }

    @Test
    void testOnBidPlaced_TestB_WinnerIsLeader_CascadeTerminates() {
        // Test B: second pass — winner is already the current leader.
        // Same field as A but now leadingBidder=bidder2, currentPrice=61000.
        // Expected: best=ab2 (70000) == leader → return immediately.
        // The challenger ab1 (60000) is outbid (< minimumNext 62000) and deactivated.
        ReflectionTestUtils.setField(auction, "minimumIncrement", 1000.0);
        ReflectionTestUtils.setField(auction, "currentPrice", 50000.0);
        ReflectionTestUtils.setField(auction, "leadingBidder", bidder2);

        AutoBid ab1 = new AutoBid(auction, bidder1, 60000.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab2 = new AutoBid(auction, bidder2, 70000.0);
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(5));

        ReflectionTestUtils.setField(auction, "currentPrice", 61000.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "bidder2Id", null, 61000.0));

        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));
        verify(spyService, times(1)).deactivateAutoBid("ab1Id");
        verify(spyService, never()).deactivateAutoBid("ab2Id");
    }

    @Test
    void testOnBidPlaced_TestC_ThreeAutoBidders_SinglePassResolution() {
        // Test C: three auto-bidders, manual leader (no AutoBid).
        // ab1.maxBid=60000, ab2.maxBid=70000, ab3.maxBid=80000.
        // currentPrice=51000, increment=1000, leadingBidder=manualBidder.
        // Expected: executeAutoBid called once for bidder3 at
        //   winningPrice = min(80000, 70000+1000) = 71000.
        // ab1 and ab2 are NOT deactivated (still viable).
        User bidder3 = new User("bidder3", "bidder3@example.com", "pw", "Bob Bidder 3", 1000.0);
        ReflectionTestUtils.setField(bidder3, "id", "bidder3Id");

        User manualBidder = new User("manual", "m@example.com", "pw", "Manual", 100.0);
        ReflectionTestUtils.setField(manualBidder, "id", "manualId");

        ReflectionTestUtils.setField(auction, "minimumIncrement", 1000.0);
        ReflectionTestUtils.setField(auction, "currentPrice", 51000.0);
        ReflectionTestUtils.setField(auction, "leadingBidder", manualBidder);

        AutoBid ab1 = new AutoBid(auction, bidder1, 60000.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(20));

        AutoBid ab2 = new AutoBid(auction, bidder2, 70000.0);
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab3 = new AutoBid(auction, bidder3, 80000.0);
        ReflectionTestUtils.setField(ab3, "id", "ab3Id");
        ReflectionTestUtils.setField(ab3, "creationDate", LocalDateTime.now().minusMinutes(5));

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2, ab3));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "manualId", null, 51000.0));

        ArgumentCaptor<String> autoBidIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bidderIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);

        verify(spyService, times(1)).executeAutoBid(
                eq("auctionId"), autoBidIdCaptor.capture(), bidderIdCaptor.capture(), amountCaptor.capture());
        assertEquals("ab3Id", autoBidIdCaptor.getValue());
        assertEquals("bidder3Id", bidderIdCaptor.getValue());
        assertEquals(71000.0, amountCaptor.getValue());

        // ab1 and ab2 are NOT deactivated — still viable for future rounds.
        verify(spyService, never()).deactivateAutoBid("ab1Id");
        verify(spyService, never()).deactivateAutoBid("ab2Id");
    }

    @Test
    void testOnBidPlaced_TestD_ThreeAutoBidders_SecondPass_WinnerLeads_CascadeStops() {
        // Test D: second pass after Test C — bidder3 is now leader at 71000.
        // Expected: best=ab3, winner == leader → return. No bid.
        // The best challenger ab2 (70000) is below minimumNext (72000) so it is deactivated.
        User bidder3 = new User("bidder3", "bidder3@example.com", "pw", "Bob Bidder 3", 1000.0);
        ReflectionTestUtils.setField(bidder3, "id", "bidder3Id");

        ReflectionTestUtils.setField(auction, "minimumIncrement", 1000.0);
        ReflectionTestUtils.setField(auction, "currentPrice", 50000.0);
        ReflectionTestUtils.setField(auction, "leadingBidder", bidder3);

        AutoBid ab1 = new AutoBid(auction, bidder1, 60000.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(20));

        AutoBid ab2 = new AutoBid(auction, bidder2, 70000.0);
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab3 = new AutoBid(auction, bidder3, 80000.0);
        ReflectionTestUtils.setField(ab3, "id", "ab3Id");
        ReflectionTestUtils.setField(ab3, "creationDate", LocalDateTime.now().minusMinutes(5));

        ReflectionTestUtils.setField(auction, "currentPrice", 71000.0);

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2, ab3));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "bidder3Id", null, 71000.0));

        verify(spyService, never()).executeAutoBid(any(), any(), any(), any(Double.class));
        verify(spyService, times(1)).deactivateAutoBid("ab2Id");
        verify(spyService, never()).deactivateAutoBid("ab1Id");
        verify(spyService, never()).deactivateAutoBid("ab3Id");
    }

    @Test
    void testOnBidPlaced_TestE_LeaderHasAutoBid_ChallengerBeats() {
        // Test E: leader has an AutoBid but a challenger beats them.
        // ab1.maxBid=90000 (bidder1, current leader),
        // ab2.maxBid=100000 (bidder2, challenger).
        // currentPrice=71000, increment=1000.
        // Expected: best=ab2, second=ab1,
        //   winningPrice = min(100000, 90000+1000) = 91000.
        // executeAutoBid called for bidder2 at 91000.
        ReflectionTestUtils.setField(auction, "minimumIncrement", 1000.0);
        ReflectionTestUtils.setField(auction, "currentPrice", 71000.0);
        ReflectionTestUtils.setField(auction, "leadingBidder", bidder1);

        AutoBid ab1 = new AutoBid(auction, bidder1, 90000.0);
        ReflectionTestUtils.setField(ab1, "id", "ab1Id");
        ReflectionTestUtils.setField(ab1, "creationDate", LocalDateTime.now().minusMinutes(10));

        AutoBid ab2 = new AutoBid(auction, bidder2, 100000.0);
        ReflectionTestUtils.setField(ab2, "id", "ab2Id");
        ReflectionTestUtils.setField(ab2, "creationDate", LocalDateTime.now().minusMinutes(5));

        when(auctionRepository.findById("auctionId")).thenReturn(Optional.of(auction));
        when(autoBidRepository.findByAuctionAndActiveTrue(auction)).thenReturn(Arrays.asList(ab1, ab2));

        AutoBidService spyService = mock(AutoBidService.class);
        ReflectionTestUtils.setField(autoBidService, "self", spyService);

        autoBidService.onBidPlaced(new BidEvent("auctionId", "Comic Sale", "sellerId", "bidder1Id", null, 71000.0));

        ArgumentCaptor<String> autoBidIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bidderIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);

        verify(spyService, times(1)).executeAutoBid(
                eq("auctionId"), autoBidIdCaptor.capture(), bidderIdCaptor.capture(), amountCaptor.capture());
        assertEquals("ab2Id", autoBidIdCaptor.getValue());
        assertEquals("bidder2Id", bidderIdCaptor.getValue());
        assertEquals(91000.0, amountCaptor.getValue());
        verify(spyService, never()).deactivateAutoBid(any());
    }
}
