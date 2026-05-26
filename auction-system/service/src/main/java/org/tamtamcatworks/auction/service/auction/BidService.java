package org.tamtamcatworks.auction.service.auction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.BidTransactionRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidTransactionRepository bidRepository;
    private final UserRepository userRepository;

    public BidService(AuctionRepository auctionRepository,
                      BidTransactionRepository bidRepository,
                      UserRepository userRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BidTransaction placeBid(String auctionId, String bidderId,
                                   double amount, BidTransaction.BidType bidType) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));

        if (!auction.isAcceptingBids())
            throw new IllegalStateException("Auction is not accepting bids.");
        if (!auction.isValidBidAmount(amount))
            throw new IllegalArgumentException("Bid amount too low.");
        if (bidder.getBalance() < amount)
            throw new IllegalArgumentException("Insufficient balance.");

        BidTransaction tx = new BidTransaction(auction, bidder, amount, bidType);
        auction.recordBid(tx);
        auctionRepository.save(auction);
        return tx;
    }

    @Transactional(readOnly = true)
    public List<BidTransaction> findByAuction(String auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        return bidRepository.findByAuctionOrderByCreationDateAsc(auction);
    }

    @Transactional(readOnly = true)
    public List<BidTransaction> findByBidder(String bidderId) {
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));
        return bidRepository.findByBidderOrderByCreationDateAsc(bidder);
    }
}
