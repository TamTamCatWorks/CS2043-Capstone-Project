package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AutoBid;
import org.tamtamcatworks.auction.model.user.User;

import java.util.List;
import java.util.Optional;

public interface AutoBidRepository extends JpaRepository<AutoBid, String> {

    List<AutoBid> findByAuctionAndActiveTrue(Auction auction);

    Optional<AutoBid> findByAuctionAndBidder(Auction auction, User bidder);
}
