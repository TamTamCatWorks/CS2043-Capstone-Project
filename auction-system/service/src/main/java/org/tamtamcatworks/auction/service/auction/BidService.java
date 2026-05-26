package org.tamtamcatworks.auction.service.auction;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.BidTransactionRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.event.BidEvent;
import org.tamtamcatworks.auction.service.mapper.BidMapper;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.BidResponse;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidTransactionRepository bidRepository;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;
    private final ApplicationEventPublisher eventPublisher;

    public BidService(AuctionRepository auctionRepository,
                      BidTransactionRepository bidRepository,
                      UserRepository userRepository,
                      BidMapper bidMapper,
                      ApplicationEventPublisher eventPublisher) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.bidMapper = bidMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BidResponse placeBid(String auctionId, String bidderId, BidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));

        if (!auction.isAcceptingBids())
            throw new IllegalStateException("Auction is not accepting bids.");
        if (!auction.isValidBidAmount(request.amount()))
            throw new IllegalArgumentException("Bid amount too low.");

        User previousLeader = auction.getLeadingBidder();
        double currentPrice = auction.getCurrentPrice();
        boolean rebiddingAgainstSelf = previousLeader != null && previousLeader.getId().equals(bidder.getId());
        double requiredAvailableBalance = rebiddingAgainstSelf
            ? request.amount() - currentPrice
            : request.amount();

        if (bidder.getBalance() < requiredAvailableBalance)
            throw new IllegalArgumentException("Insufficient available balance.");

        if (rebiddingAgainstSelf) {
            bidder.holdFunds(requiredAvailableBalance);
        } else {
            bidder.holdFunds(request.amount());
            if (previousLeader != null) {
                previousLeader.releaseHeldFunds(currentPrice);
                userRepository.save(previousLeader);
            }
        }

        BidTransaction tx = bidMapper.toEntity(request, auction, bidder);
        auction.recordBid(tx);
        auctionRepository.save(auction);
        userRepository.save(bidder);

        eventPublisher.publishEvent(new BidEvent(
            auction.getId(),
            auction.getTitle(),
            auction.getSeller().getId(),
            bidderId,
            previousLeader != null ? previousLeader.getId() : null,
            request.amount()
        ));

        return bidMapper.toResponse(tx);
    }

    @Transactional(readOnly = true)
    public List<BidResponse> findByAuction(String auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        return bidRepository.findByAuctionOrderByCreationDateAsc(auction)
            .stream()
            .map(bidMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BidResponse> findByBidder(String bidderId) {
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));
        return bidRepository.findByBidderOrderByCreationDateAsc(bidder)
            .stream()
            .map(bidMapper::toResponse)
            .collect(Collectors.toList());
    }
}
