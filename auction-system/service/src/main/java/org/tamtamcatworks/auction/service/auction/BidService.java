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
import org.tamtamcatworks.auction.service.event.UserStateEvent;
import org.tamtamcatworks.auction.service.mapper.BidMapper;
import org.tamtamcatworks.auction.service.mapper.UserMapper;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.BidResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidTransactionRepository bidRepository;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AntiSnipeProperties antiSnipe;

    public BidService(AuctionRepository auctionRepository,
                      BidTransactionRepository bidRepository,
                      UserRepository userRepository,
                      BidMapper bidMapper,
                      UserMapper userMapper,
                      ApplicationEventPublisher eventPublisher,
                      AntiSnipeProperties antiSnipe) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.bidMapper = bidMapper;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.antiSnipe = antiSnipe;
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

        LocalDateTime snipeWindowStart = auction.getEndTime()
                .minusSeconds(antiSnipe.windowSeconds());

        if (!LocalDateTime.now().isBefore(snipeWindowStart)) {
            auction.extendEndTime(antiSnipe.extensionSeconds());
        }

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

        eventPublisher.publishEvent(new UserStateEvent(bidder.getId(), userMapper.toResponse(bidder)));
        if (previousLeader != null && !previousLeader.getId().equals(bidder.getId())) {
            eventPublisher.publishEvent(new UserStateEvent(previousLeader.getId(), userMapper.toResponse(previousLeader)));
        }

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
