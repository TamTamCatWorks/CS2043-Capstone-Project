package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.user.User;
import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, String> {
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findBySeller(User seller);
}