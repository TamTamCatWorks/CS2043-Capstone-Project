package org.tamtamcatworks.auction.service.auction;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.event.AuctionEvent;
import org.tamtamcatworks.auction.service.event.UserStateEvent;
import org.tamtamcatworks.auction.service.item.ItemService;
import org.tamtamcatworks.auction.service.mapper.AuctionMapper;
import org.tamtamcatworks.auction.service.mapper.UserMapper;
import org.tamtamcatworks.auction.shared.request.AuctionRequest;
import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final AuctionMapper auctionMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AuctionService(AuctionRepository auctionRepository,
                          UserRepository userRepository,
                          ItemService itemService,
                          AuctionMapper auctionMapper,
                          UserMapper userMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.itemService = itemService;
        this.auctionMapper = auctionMapper;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
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

        if (!seller.isActive()) {
            throw new IllegalStateException("Suspended users cannot create auctions.");
        }
        
        return auctionRepository.save(auctionMapper.toEntity(req, seller, item));
    }

    @Transactional
    public Auction create(String sellerId, String itemId, String title,
                          double startingPrice, LocalDateTime startTime, LocalDateTime endTime) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!seller.isActive()) {
            throw new IllegalStateException("Suspended users cannot create auctions.");
        }

        Item item = itemService.findById(itemId);
        return auctionRepository.save(new Auction(title, seller, item, startingPrice, startTime, endTime));
    }

    /**
     * Creates an auction for an existing item using the request DTO form.
     * Use this overload from API/controller paths; keep the parameter-based overload for internal callers.
     */
    @Transactional
    public AuctionResponse create(@NonNull String sellerId, @NonNull AuctionRequest request) {
        Auction createdAuction = create(
            sellerId,
            request.itemId(),
            request.title(),
            request.startingPrice(),
            request.startTime(),
            request.endTime()
        );
        return auctionMapper.toResponse(createdAuction);
    }

    @Transactional
    public Auction open(String auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found"));

        if (!auction.getSeller().isActive()) {

            throw new IllegalStateException("Suspended users cannot open auctions.");
        }

        auction.open();
        Auction saved = auctionRepository.save(auction);
        eventPublisher.publishEvent(new AuctionEvent(
            saved.getId(), saved.getTitle(),
            saved.getSeller().getId(), null, saved.getCurrentPrice(), saved.getStatus(), null));
        return saved;
    }

    @Transactional
    public Auction close(String auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found"));
        User leadingBidder = auction.getLeadingBidder();
        double currentPrice = auction.getCurrentPrice();
        auction.close();
        if (leadingBidder != null && currentPrice > 0) {
            leadingBidder.consumeHeldFunds(currentPrice);
            userRepository.save(leadingBidder);
            eventPublisher.publishEvent(new UserStateEvent(
                leadingBidder.getId(),
                userMapper.toResponse(leadingBidder)
            ));

            // Deposit the money into the seller's account
            User seller = auction.getSeller();
            seller.addBalance(currentPrice);
            userRepository.save(seller);
            eventPublisher.publishEvent(new UserStateEvent(
                seller.getId(),
                userMapper.toResponse(seller)
            ));
        }
        Auction saved = auctionRepository.save(auction);
        eventPublisher.publishEvent(new AuctionEvent(
            saved.getId(), saved.getTitle(),
            saved.getSeller().getId(),
            saved.getLeadingBidder() != null ? saved.getLeadingBidder().getId() : null,
            saved.getCurrentPrice(), saved.getStatus(), null));
        return saved;
    }

    @Transactional
    public Auction cancel(String auctionId, String reason) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found"));
        User leadingBidder = auction.getLeadingBidder();
        double currentPrice = auction.getCurrentPrice();
        auction.cancel(reason);
        if (leadingBidder != null && currentPrice > 0) {
            leadingBidder.releaseHeldFunds(currentPrice);
            userRepository.save(leadingBidder);
            eventPublisher.publishEvent(new UserStateEvent(
                leadingBidder.getId(),
                userMapper.toResponse(leadingBidder)
            ));
        }
        Auction saved = auctionRepository.save(auction);
        eventPublisher.publishEvent(new AuctionEvent(
            saved.getId(), saved.getTitle(),
            saved.getSeller().getId(),
            saved.getLeadingBidder() != null ? saved.getLeadingBidder().getId() : null,
            saved.getCurrentPrice(), saved.getStatus(), reason));
        return saved;
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

    @Transactional(readOnly = true)
    public List<AuctionResponse> searchResponses(String keyword,
                                                 AuctionStatus status,
                                                 String category) {
        String normalizedKeyword = keyword != null ? keyword.trim() : "";
        String normalizedCategory = category != null ? category.trim() : "";

        return auctionRepository.search(normalizedKeyword, status).stream()
            .filter(auction -> matchesCategory(auction, normalizedCategory))
            .map(auctionMapper::toResponse)
            .toList();
    }

    @Transactional
    public AuctionResponse createByRequest(String sellerId, CreateAuctionRequest request) {
        Auction createdAuction = createWithItem(sellerId, request);
        return auctionMapper.toResponse(createdAuction);
    }

    @Transactional(readOnly = true)
    public AuctionResponse findResponseById(@NonNull String auctionId) {
        return auctionMapper.toResponse(findById(auctionId));
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> findResponsesByStatus(@NonNull AuctionStatus status) {
        return findByStatus(status).stream().map(auctionMapper::toResponse).toList();
    }

    @Transactional
    public AuctionResponse openById(@NonNull String auctionId) {
        return auctionMapper.toResponse(open(auctionId));
    }

    @Transactional
    public AuctionResponse closeById(@NonNull String auctionId) {
        return auctionMapper.toResponse(close(auctionId));
    }

    @Transactional
    public AuctionResponse cancelById(@NonNull String auctionId, @NonNull String reason) {
        return auctionMapper.toResponse(cancel(auctionId, reason));
    }

    private boolean matchesCategory(Auction auction, String category) {
        if (category == null || category.isBlank() || "all categories".equalsIgnoreCase(category)) {
            return true;
        }

        String itemType = auction.getItem() != null ? auction.getItem().getClass().getSimpleName() : "";
        return itemType.equalsIgnoreCase(category);
    }

    @Transactional(readOnly = true)
    public org.tamtamcatworks.auction.shared.response.PageResponse<AuctionResponse> searchResponsesPage(String keyword,
                                                    AuctionStatus status,
                                                    String category,
                                                    Pageable pageable) {
        String normalizedKeyword = keyword != null ? keyword.trim() : "";
        String normalizedCategory = category != null ? category.trim() : "";

        Class<? extends Item> itemTypeClass = null;
        if (normalizedCategory != null && !normalizedCategory.isBlank() && !"all categories".equalsIgnoreCase(normalizedCategory)) {
            try {
                String className = "org.tamtamcatworks.auction.model.item." + normalizedCategory;
                @SuppressWarnings("unchecked")
                Class<? extends Item> cls = (Class<? extends Item>) Class.forName(className);
                itemTypeClass = cls;
            } catch (ClassNotFoundException e) {
                // if class isn't found, leave itemTypeClass null (no type filter)
            }
        }

        Page<Auction> page = auctionRepository.searchPaged(normalizedKeyword, status, itemTypeClass, pageable);
        return auctionMapper.toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAllAuctions() {

        return auctionRepository.findAll()
            .stream()
            .map(auctionMapper::toResponse)
            .toList();
    }
}
