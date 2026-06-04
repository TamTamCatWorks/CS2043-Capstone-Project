package org.tamtamcatworks.auction.service.auction;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;

@Component
public class AuctionScheduler {

  private static final Logger log = LoggerFactory.getLogger(AuctionScheduler.class);

  private final AuctionService auctionService;

  public AuctionScheduler(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  @Scheduled(fixedRate = 30_000) // runs every 30 seconds
  @Transactional
  public void closeExpiredAuctions() {
    List<Auction> expired =
        auctionService.findByStatus(AuctionStatus.ACTIVE).stream()
            .filter(auction -> auction.getEndTime().isBefore(LocalDateTime.now()))
            .toList();

    for (Auction auction : expired) {
      try {
        auctionService.close(auction.getId());
        log.info("Auto-closed auction: {} ({})", auction.getTitle(), auction.getId());
      } catch (IllegalStateException e) {
        log.warn("Could not auto-close auction {}: {}", auction.getId(), e.getMessage());
      }
    }
  }

  @Scheduled(fixedRate = 30_000) // runs every 30 seconds
  @Transactional
  public void openPendingAuctions() {
    List<Auction> pending =
        auctionService.findByStatus(AuctionStatus.PENDING).stream()
            .filter(auction -> auction.getStartTime().isBefore(LocalDateTime.now()))
            .toList();

    for (Auction auction : pending) {
      try {
        auctionService.open(auction.getId());
        log.info("Auto-opened auction: {} ({})", auction.getTitle(), auction.getId());
      } catch (IllegalStateException e) {
        log.warn("Could not auto-open auction {}: {}", auction.getId(), e.getMessage());
      }
    }
  }
}
