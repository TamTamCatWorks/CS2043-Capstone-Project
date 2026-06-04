package org.tamtamcatworks.auction.service.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.event.AuctionEvent;
import org.tamtamcatworks.auction.service.event.UserStateEvent;
import org.tamtamcatworks.auction.service.item.ItemService;
import org.tamtamcatworks.auction.service.mapper.AuctionMapper;
import org.tamtamcatworks.auction.service.mapper.UserMapper;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

  @Mock private AuctionRepository auctionRepository;

  @Mock private UserRepository userRepository;

  @Mock private ItemService itemService;

  @Mock private AuctionMapper auctionMapper;

  @Mock private UserMapper userMapper;

  @Mock private ApplicationEventPublisher eventPublisher;

  private AuctionService auctionService;

  private User seller;
  private Art item;
  private Auction auction;

  @BeforeEach
  void setUp() {
    auctionService =
        new AuctionService(
            auctionRepository,
            userRepository,
            itemService,
            auctionMapper,
            userMapper,
            eventPublisher);

    seller = new User("seller", "seller@example.com", "pw", "John Seller", 1000.0);
    item =
        new Art(
            "Starry Paint",
            "Painting style replica",
            200.0,
            ItemCondition.GOOD,
            "img1",
            seller,
            "Vincent",
            2024,
            "Oil",
            "50x50",
            false);
    auction =
        new Auction(
            "Rare Art Masterpiece",
            seller,
            item,
            200.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(2));
  }

  @Test
  void testCreateAuctionSuccess() {
    when(userRepository.findById("seller123")).thenReturn(Optional.of(seller));
    when(itemService.findById("item123")).thenReturn(item);
    when(auctionRepository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

    Auction created =
        auctionService.create(
            "seller123",
            "item123",
            "Rare Art Masterpiece",
            200.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(2));
    assertNotNull(created);
    assertEquals("Rare Art Masterpiece", created.getTitle());
    assertEquals(200.0, created.getStartingPrice());
  }

  @Test
  void testOpenAuction() {
    when(auctionRepository.findById("auc123")).thenReturn(Optional.of(auction));
    when(auctionRepository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

    Auction opened = auctionService.open("auc123");
    assertEquals(AuctionStatus.ACTIVE, opened.getStatus());
    verify(eventPublisher, times(1)).publishEvent(any(AuctionEvent.class));
  }

  @Test
  void testCloseAuctionWithWinner() {
    User winner = new User("winner", "winner@example.com", "pw", "Winner Bob", 5000.0);
    winner.holdFunds(1500.0);
    auction.open();
    ReflectionTestUtils.setField(auction, "leadingBidder", winner);
    ReflectionTestUtils.setField(auction, "currentPrice", 1500.0);

    when(auctionRepository.findById("auc123")).thenReturn(Optional.of(auction));
    when(auctionRepository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    Auction closed = auctionService.close("auc123");
    assertEquals(AuctionStatus.CLOSED, closed.getStatus());
    assertEquals(3500.0, winner.getBalance()); // 5000 - 1500
    assertEquals(0.0, winner.getHoldBalance()); // held funds consumed
    assertEquals(2500.0, seller.getBalance()); // seller got 1000 + 1500

    verify(eventPublisher, times(2)).publishEvent(any(UserStateEvent.class));
    verify(eventPublisher, times(1)).publishEvent(any(AuctionEvent.class));
  }

  @Test
  void testCancelAuctionWithWinner() {
    User winner = new User("winner", "winner@example.com", "pw", "Winner Bob", 5000.0);
    winner.holdFunds(1500.0);
    auction.open();
    ReflectionTestUtils.setField(auction, "leadingBidder", winner);
    ReflectionTestUtils.setField(auction, "currentPrice", 1500.0);

    when(auctionRepository.findById("auc123")).thenReturn(Optional.of(auction));
    when(auctionRepository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    Auction cancelled = auctionService.cancel("auc123", "Reason for cancel");
    assertEquals(AuctionStatus.CANCELLED, cancelled.getStatus());
    assertEquals(5000.0, winner.getBalance()); // held funds refunded
    assertEquals(0.0, winner.getHoldBalance());

    verify(eventPublisher, times(1)).publishEvent(any(UserStateEvent.class));
    verify(eventPublisher, times(1)).publishEvent(any(AuctionEvent.class));
  }

  @Test
  void testSearchResponses() {
    when(auctionRepository.search("Art", AuctionStatus.ACTIVE)).thenReturn(List.of(auction));

    List<AuctionResponse> results =
        auctionService.searchResponses("Art", AuctionStatus.ACTIVE, "Art");
    assertNotNull(results);
  }
}
