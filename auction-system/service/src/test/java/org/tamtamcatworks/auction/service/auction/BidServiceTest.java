package org.tamtamcatworks.auction.service.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.Other;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.BidTransactionRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.event.BidEvent;
import org.tamtamcatworks.auction.service.event.UserStateEvent;
import org.tamtamcatworks.auction.service.mapper.BidMapper;
import org.tamtamcatworks.auction.service.mapper.UserMapper;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.BidResponse;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

  @Mock private AuctionRepository auctionRepository;

  @Mock private BidTransactionRepository bidRepository;

  @Mock private UserRepository userRepository;

  @Mock private BidMapper bidMapper;

  @Mock private UserMapper userMapper;

  @Mock private ApplicationEventPublisher eventPublisher;

  private BidService bidService;

  private User seller;
  private User bidder;
  private Auction auction;
  private AntiSnipeProperties antiSnipe;

  @BeforeEach
  void setUp() {
    antiSnipe = new AntiSnipeProperties(60, 120);
    bidService =
        new BidService(
            auctionRepository,
            bidRepository,
            userRepository,
            bidMapper,
            userMapper,
            eventPublisher,
            antiSnipe);

    seller = new User("seller", "seller@example.com", "pw", "John Seller", 100.0);
    bidder = new User("bidder", "bidder@example.com", "pw", "Bob Bidder", 1000.0);
    Other item = new Other("Comic", "Rare book", 100.0, ItemCondition.GOOD, "img", seller);
    auction =
        new Auction(
            "Comic Sale",
            seller,
            item,
            100.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1));
    ReflectionTestUtils.setField(auction, "minimumIncrement", 1.0);
  }

  @Test
  void testPlaceBidSuccess() {
    auction.open();
    BidRequest request = new BidRequest(120.0, "MANUAL");
    BidTransaction tx = new BidTransaction(auction, bidder, 120.0, BidTransaction.BidType.MANUAL);

    when(auctionRepository.findById("auc123")).thenReturn(Optional.of(auction));
    when(userRepository.findById("bidder123")).thenReturn(Optional.of(bidder));
    when(bidMapper.toEntity(request, auction, bidder)).thenReturn(tx);
    when(bidMapper.toResponse(tx))
        .thenReturn(
            new BidResponse(
                "bid123",
                "auc123",
                "bidder123",
                "Bob Bidder",
                120.0,
                "MANUAL",
                LocalDateTime.now()));

    BidResponse response = bidService.placeBid("auc123", "bidder123", request);
    assertNotNull(response);
    assertEquals(120.0, response.amount());
    assertEquals(120.0, bidder.getHoldBalance());
    assertEquals(880.0, bidder.getBalance());

    verify(auctionRepository, times(1)).save(auction);
    verify(userRepository, times(1)).save(bidder);
    verify(eventPublisher, times(1)).publishEvent(any(BidEvent.class));
    verify(eventPublisher, times(1)).publishEvent(any(UserStateEvent.class));
  }

  @Test
  void testPlaceBidInsufficientBalanceThrowsException() {
    auction.open();
    BidRequest request = new BidRequest(1200.0, "MANUAL");

    when(auctionRepository.findById("auc123")).thenReturn(Optional.of(auction));
    when(userRepository.findById("bidder123")).thenReturn(Optional.of(bidder));

    assertThrows(
        IllegalArgumentException.class, () -> bidService.placeBid("auc123", "bidder123", request));
  }

  @Test
  void testPlaceBidSnipeExtension() {
    auction.open();
    // Set end time to 30 seconds from now (which is within the 60-second window)
    LocalDateTime originalEndTime = LocalDateTime.now().plusSeconds(30);
    auction.extendEndTime(0); // reset if needed

    // Use reflection or constructor to recreate custom endTime
    Auction customAuction =
        new Auction(
            "Comic Sale",
            seller,
            auction.getItem(),
            100.0,
            LocalDateTime.now().minusHours(1),
            originalEndTime);
    ReflectionTestUtils.setField(customAuction, "minimumIncrement", 1.0);
    customAuction.open();

    BidRequest request = new BidRequest(150.0, "MANUAL");
    BidTransaction tx =
        new BidTransaction(customAuction, bidder, 150.0, BidTransaction.BidType.MANUAL);

    when(auctionRepository.findById("auc123")).thenReturn(Optional.of(customAuction));
    when(userRepository.findById("bidder123")).thenReturn(Optional.of(bidder));
    when(bidMapper.toEntity(request, customAuction, bidder)).thenReturn(tx);

    bidService.placeBid("auc123", "bidder123", request);

    // End time should be extended by 120 seconds (extensionSeconds of antiSnipe)
    assertTrue(customAuction.getEndTime().isAfter(originalEndTime));
  }
}
