package org.tamtamcatworks.auction.service.auction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuctionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuctionScheduler.class);

    private final AuctionRepository auctionRepository;

    public AuctionScheduler(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Scheduled(fixedRate = 30_000)   // runs every 30 seconds
    @Transactional
    public void closeExpiredAuctions() {
        List<Auction> expired = auctionRepository
                .findByStatusAndEndTimeBefore(AuctionStatus.ACTIVE, LocalDateTime.now());

        for (Auction auction : expired) {
            try {
                auction.close();
                auctionRepository.save(auction);
                log.info("Auto-closed auction: {} ({})", auction.getTitle(), auction.getId());
            } catch (IllegalStateException e) {
                log.warn("Could not auto-close auction {}: {}", auction.getId(), e.getMessage());
            }
        }
    }
}
