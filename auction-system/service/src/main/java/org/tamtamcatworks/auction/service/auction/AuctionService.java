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
import org.tamtamcatworks.auction.service.mapper.AuctionMapper;
import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final AuctionMapper auctionMapper;

    public AuctionService(AuctionRepository auctionRepository,
                          UserRepository userRepository,
                          ItemService itemService,
                          AuctionMapper auctionMapper) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.itemService = itemService;
        this.auctionMapper = auctionMapper;
    }

    @Transactional
    public Auction createWithItem(String sellerId, CreateAuctionRequest req) {
        ItemRequest itemRequest = new ItemRequest(
            req.item().itemType(),
            req.item().name(),
            req.item().description(),
            req.item().startingPrice(),
            req.item().condition(),
            sellerId,
            req.item().imageUrl(),
            req.item().details()
        );

        Item item = itemService.create(itemRequest);
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        return auctionRepository.save(auctionMapper.toEntity(req, seller, item));
    }

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
        Auction auction = auctionRepository.findById(auctionId)
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
    public List<Auction> findByStatus(AuctionStatus status) {
        return Optional.ofNullable(auctionRepository.findByStatus(status))
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
    }

    @Transactional(readOnly = true)
    public List<Auction> findBySeller(User seller) {
        return Optional.ofNullable(auctionRepository.findBySeller(seller))
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
    }

    @Transactional
    public AuctionResponse createByRequest(CreateAuctionRequest request) {
        Item item = itemService.create(request.item());
        User seller = userRepository.findById(request.item().sellerId())
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        Auction auction = auctionMapper.toEntity(request, seller, item);
        Auction savedAuction = auctionRepository.save(auction);
        return auctionMapper.toResponse(savedAuction);
    }

    public AuctionResponse toResponse(Auction auction) {
        return auctionMapper.toResponse(auction);
    }

    public List<AuctionResponse> toResponses(List<Auction> auctions) {
        return auctions.stream().map(auctionMapper::toResponse).toList();
    }
}
