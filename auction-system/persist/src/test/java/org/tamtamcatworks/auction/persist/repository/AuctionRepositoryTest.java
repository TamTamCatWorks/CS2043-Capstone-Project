package org.tamtamcatworks.auction.persist.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.Electronics;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.user.User;

@DataJpaTest
class AuctionRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Autowired private ItemRepository itemRepository;

  @Autowired private AuctionRepository auctionRepository;

  private User seller;
  private Art art;
  private Electronics electronics;
  private Auction auctionArt;
  private Auction auctionElec;

  @BeforeEach
  void setUp() {
    seller = new User("sellerX", "sellerx@example.com", "pw", "John Seller", 1000.0);
    userRepository.save(seller);

    art =
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
    itemRepository.save(art);

    electronics =
        new Electronics(
            "LG Screen",
            "4K monitor screen",
            300.0,
            ItemCondition.NEW,
            "img2",
            seller,
            "LG",
            "27GP850",
            12);
    itemRepository.save(electronics);

    LocalDateTime now = LocalDateTime.now();
    auctionArt =
        new Auction("Rare Art Masterpiece", seller, art, 200.0, now.minusDays(1), now.plusDays(2));
    auctionArt.open();
    auctionRepository.save(auctionArt);

    auctionElec =
        new Auction(
            "LG Monitor Sale", seller, electronics, 300.0, now.minusDays(2), now.minusMinutes(5));
    auctionRepository.save(auctionElec);
  }

  @Test
  void testFindByStatusAndEndTimeBefore() {
    LocalDateTime checkTime = LocalDateTime.now();
    List<Auction> expired =
        auctionRepository.findByStatusAndEndTimeBefore(AuctionStatus.PENDING, checkTime);
    assertEquals(1, expired.size());
    assertEquals("LG Monitor Sale", expired.get(0).getTitle());
  }

  @Test
  void testFindByStatus() {
    List<Auction> active = auctionRepository.findByStatus(AuctionStatus.ACTIVE);
    assertEquals(1, active.size());
    assertEquals("Rare Art Masterpiece", active.get(0).getTitle());
  }

  @Test
  void testFindBySeller() {
    List<Auction> sellerAuctions = auctionRepository.findBySeller(seller);
    assertEquals(2, sellerAuctions.size());
  }

  @Test
  void testSearchByKeyword() {
    List<Auction> searchTitle = auctionRepository.search("Monitor", null);
    assertEquals(1, searchTitle.size());
    assertEquals("LG Monitor Sale", searchTitle.get(0).getTitle());

    List<Auction> searchDesc = auctionRepository.search("replica", null);
    assertEquals(1, searchDesc.size());
    assertEquals("Rare Art Masterpiece", searchDesc.get(0).getTitle());

    List<Auction> searchSeller = auctionRepository.search("John", null);
    assertEquals(2, sellerAuctionsCount(searchSeller));
  }

  @Test
  void testSearchPaged() {
    Page<Auction> pagedResult =
        auctionRepository.searchPaged("", null, Art.class, PageRequest.of(0, 10));
    assertEquals(1, pagedResult.getTotalElements());
    assertEquals("Rare Art Masterpiece", pagedResult.getContent().get(0).getTitle());
  }

  private int sellerAuctionsCount(List<Auction> list) {
    return list.size();
  }
}
