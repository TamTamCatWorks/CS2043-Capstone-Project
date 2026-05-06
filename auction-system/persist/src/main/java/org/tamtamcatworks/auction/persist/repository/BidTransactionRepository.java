package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.user.User;
import java.util.List;

public interface BidTransactionRepository extends JpaRepository<BidTransaction, String> {
    List<BidTransaction> findByBidderOrderByCreationDateAsc(User bidder);
}