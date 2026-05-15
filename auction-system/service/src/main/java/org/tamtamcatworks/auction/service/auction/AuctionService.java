package org.tamtamcatworks.auction.service.auction;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.item.ItemService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AuctionService {
    private final AuctionRepository auctionRepository;
   // private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemService itemService;

    public AuctionService(AuctionRepository auctionRepository, UserRepository userRepository, ItemService itemService) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.itemService = itemService;
    }

//  @Transactional
//  public Auction createWithItem(String sellerId, CreateAuctionRequest req) {
//      Item item = itemService.create(req.item());
//      User seller = userRepository.findById(sellerId)
//              .orElseThrow(() -> new NoSuchElementException("User not found"));
//      return auctionRepository.save(new Auction(req.title(), seller, item,
//              req.item().startingPrice(), req.startTime(), req.endTime()));
//  }

    @Transactional
    public Auction create(String sellerId, String itemId, String title,
                        double startingPrice, LocalDateTime startTime, LocalDateTime endTime) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Item item = itemService.findById(itemId);
        return auctionRepository.save(new Auction(title, seller, item, startingPrice, startTime, endTime));
    }

    @Transactional
    public Auction open(String auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("Auction not found"));
        auction.open();
        return auctionRepository.save(auction);
    }

    @Transactional
    public Auction close(String auctionId) {
        Auction auction =  auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("Auction not found"));
        auction.close();
        return auctionRepository.save(auction);
    }

    @Transactional
    public Auction cancel(String auctionId, String reason) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("Auction not found"));
        auction.cancel(reason);
        return auctionRepository.save(auction);
    }

    @Transactional(readOnly = true)
    public Auction findById(String auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("Auction not found."));
    }

    @Transactional(readOnly = true)
    public List<Auction> findByStatus(AuctionStatus status ) {
        return Optional.ofNullable(auctionRepository.findByStatus(status))
                .orElseThrow(() -> new NoSuchElementException("Auction not found."));
    }

    @Transactional(readOnly = true)
    public List<Auction> findBySeller(User seller) {
        return Optional.ofNullable(auctionRepository.findBySeller(seller))
                .orElseThrow(() -> new NoSuchElementException("Auction not found."));
    }
}
