package org.tamtamcatworks.auction.service.auction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AutoBid;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.AutoBidRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.event.BidEvent;
import org.tamtamcatworks.auction.shared.request.AutoBidRequest;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.AutoBidResponse;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 * Dịch vụ đấu giá tự động (Auto-Bidding).
 *
 * <p>THUẬT TOÁN:
 * <ol>
 *   <li>Sau khi một bid được chấp nhận và commit ({@code BidEvent}), service lắng nghe event.</li>
 *   <li>Lấy tất cả auto-bid còn hiệu lực của phiên đó, ngoại trừ người đang dẫn đầu.</li>
 *   <li>Đưa vào {@link PriorityQueue} sắp xếp giảm dần theo {@code maxBid}
 *       — người sẵn sàng trả cao nhất được ưu tiên cao nhất.</li>
 *   <li>Người đứng đầu queue tự động đặt giá {@code currentPrice + increment}
 *       nếu vẫn còn trong ngưỡng {@code maxBid}.</li>
 *   <li>Bid tự động kích hoạt {@code BidEvent} mới → vòng lặp tiếp tục
 *       đến khi không còn ai có thể đấu.</li>
 * </ol>
 *
 * <p>AUTO-BID POLICY: mỗi user chỉ có một auto-bid trên một phiên (upsert).
 */
@Service
public class AutoBidService {

    private final AutoBidRepository autoBidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final BidService bidService;

    public AutoBidService(AutoBidRepository autoBidRepository,
                          AuctionRepository auctionRepository,
                          UserRepository userRepository,
                          BidService bidService) {
        this.autoBidRepository = autoBidRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.bidService = bidService;
    }

    /**
     * Đăng ký hoặc cập nhật auto-bid cho một phiên đấu giá.
     * Nếu đã tồn tại, tự động cập nhật (upsert).
     *
     * @param auctionId ID phiên đấu giá
     * @param bidderId  ID người dùng
     * @param request   cấu hình maxBid và increment
     * @return thông tin auto-bid đã lưu
     */
    @Transactional
    public AutoBidResponse register(String auctionId, String bidderId, AutoBidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));

        if (!auction.isAcceptingBids()) {
            throw new IllegalStateException("Auction is not accepting bids.");
        }
        if (request.maxBid() <= auction.getCurrentPrice()) {
            throw new IllegalArgumentException(
                "maxBid phải lớn hơn giá hiện tại: " + auction.getCurrentPrice());
        }
        if (bidder.getId().equals(auction.getSeller().getId())) {
            throw new IllegalStateException("Seller không thể tự auto-bid trên phiên của mình.");
        }

        AutoBid autoBid = autoBidRepository.findByAuctionAndBidder(auction, bidder)
            .map(existing -> {
                existing.update(request.maxBid());
                return existing;
            })
            .orElseGet(() -> new AutoBid(auction, bidder, request.maxBid()));

        return toResponse(autoBidRepository.save(autoBid));
    }

    /**
     * Hủy auto-bid của một user trên một phiên đấu giá.
     *
     * @param auctionId ID phiên đấu giá
     * @param bidderId  ID người dùng
     */
    @Transactional
    public void cancel(String auctionId, String bidderId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));

        autoBidRepository.findByAuctionAndBidder(auction, bidder).ifPresent(ab -> {
            ab.deactivate();
            autoBidRepository.save(ab);
        });
    }

    /**
     * Lấy thông tin auto-bid hiện tại của một user.
     *
     * @param auctionId ID phiên đấu giá
     * @param bidderId  ID người dùng
     * @return auto-bid response
     */
    @Transactional(readOnly = true)
    public AutoBidResponse getByAuctionAndBidder(String auctionId, String bidderId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));

        return autoBidRepository.findByAuctionAndBidder(auction, bidder)
            .map(this::toResponse)
            .orElseThrow(() -> new NoSuchElementException("Không có auto-bid nào cho phiên này."));
    }

    /**
     * Lắng nghe BidEvent sau khi transaction commit, sau đó kích hoạt auto-bid
     * cho người cạnh tranh tốt nhất (cao nhất trong PriorityQueue).
     *
     * <p>Dùng {@code AFTER_COMMIT} để đảm bảo đọc đúng giá hiện tại
     * từ transaction vừa commit trước đó.
     *
     * @param event thông tin bid vừa được chấp nhận
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBidPlaced(BidEvent event) {
        Auction auction = auctionRepository.findById(event.auctionId()).orElse(null);
        if (auction == null || !auction.isAcceptingBids()) return;

        List<AutoBid> candidates = autoBidRepository.findByAuctionAndActiveTrue(auction);

        PriorityQueue<AutoBid> queue = new PriorityQueue<>(
                Comparator.comparingDouble(AutoBid::getMaxBid).reversed()
        );
        for (AutoBid ab : candidates) {
            if (!ab.getBidder().getId().equals(event.bidderId())) {
                queue.offer(ab);
            }
        }
        if (queue.isEmpty()) return;

        AutoBid best = queue.poll();
        double nextBid = auction.getCurrentPrice() + auction.getMinimumIncrement();

        if (nextBid > best.getMaxBid()) {
            best.deactivate();
            autoBidRepository.save(best);
            return;
        }

        try {
            bidService.placeBid(event.auctionId(), best.getBidder().getId(),
                    new BidRequest(nextBid, "AUTO"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            best.deactivate();
            autoBidRepository.save(best);
        }
    }

    private AutoBidResponse toResponse(AutoBid ab) {
        return new AutoBidResponse(
                ab.getId(),
                ab.getAuction().getId(),
                ab.getBidder().getId(),
                ab.getMaxBid(),
                ab.getAuction().getMinimumIncrement(),  // derived, not stored
                ab.isActive()
        );
    }
}
