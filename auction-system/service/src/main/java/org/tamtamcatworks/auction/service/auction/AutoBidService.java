package org.tamtamcatworks.auction.service.auction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tamtamcatworks.auction.model.BaseEntity;
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
 * <p>THUẬT TOÁN (single-pass proxy resolution for N users):
 * <ol>
 *   <li>Sau khi một bid được chấp nhận và commit ({@code BidEvent}), service lắng nghe event.</li>
 *   <li>Thêm <b>tất cả</b> auto-bid còn hiệu lực — kể cả người đang dẫn đầu — vào
 *       {@link PriorityQueue} sắp xếp giảm dần theo {@code maxBid},
 *       tiebreaker: {@code creationDate ASC} (đăng ký sớm hơn được ưu tiên).</li>
 *   <li>Poll {@code best} = rank 1.</li>
 *   <li><b>Case 3</b>: nếu {@code best.maxBid < currentPrice + increment} →
 *       deactivate và dừng.</li>
 *   <li><b>Tie drain</b>: deactivate tất cả peer cùng {@code maxBid} với best trong
 *       một lần duy nhất — không cascade.</li>
 *   <li>Tính {@code winningPrice}:
 *     <ul>
 *       <li>Không có second → {@code currentPrice + increment} (Case 1).</li>
 *       <li>Có second → {@code min(best.maxBid, second.maxBid + increment)} (Case 2).</li>
 *     </ul>
 *   </li>
 *   <li><b>Cascade termination</b>: nếu {@code best} đã là người dẫn đầu hiện tại
 *       → họ đang giữ vị trí đúng giá, không cần đặt bid mới → return.
 *       Điều này đảm bảo cascade kết thúc sau tối đa 2 lần gọi.</li>
 *   <li>Ngược lại → {@link #executeAutoBid} cho best tại {@code winningPrice}.</li>
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
    private final ApplicationEventPublisher eventPublisher;
    private AutoBidService self;

    public AutoBidService(AutoBidRepository autoBidRepository,
                          AuctionRepository auctionRepository,
                          UserRepository userRepository,
                          BidService bidService,
                          ApplicationEventPublisher eventPublisher) {
        this.autoBidRepository = autoBidRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.bidService = bidService;
        this.eventPublisher = eventPublisher;
    }

    @Autowired
    public void setSelf(@Lazy AutoBidService self) {
        this.self = self;
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

        // ── Eligibility guards (mirror BidService.placeBid order) ────────────
        if (!bidder.isActive()) {
            throw new IllegalStateException("Suspended users cannot register auto-bids.");
        }
        if (!auction.getSeller().isActive()) {
            throw new IllegalStateException("This auction belongs to a suspended seller.");
        }
        if (bidder.getBalance() < request.maxBid()) {
            throw new IllegalArgumentException("Insufficient balance to cover declared maxBid.");
        }
        // ── Domain-specific guards ────────────────────────────────────────────
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

        // Track which upsert path was taken so post-save dispatch can differ.
        boolean[] isNew = {false};
        AutoBid autoBid = autoBidRepository.findByAuctionAndBidder(auction, bidder)
            .map(existing -> {
                existing.update(request.maxBid());
                return existing;
            })
            .orElseGet(() -> {
                isNew[0] = true;
                return new AutoBid(auction, bidder, request.maxBid());
            });

        AutoBid saved = autoBidRepository.save(autoBid);

        // ── Immediate-bid dispatch ─────────────────────────────────────────────
        // Determine whether this bidder currently holds the lead.
        String leaderId = auction.getLeadingBidder() != null
                ? auction.getLeadingBidder().getId() : null;
        boolean isCurrentLeader = bidderId.equals(leaderId);
        double minimumNext = auction.getCurrentPrice() + auction.getMinimumIncrement();

        if (!isCurrentLeader) {
            // Non-leader (fresh registration or update): bid immediately at the
            // minimum next price, but only if the declared ceiling covers it.
            if (saved.getMaxBid() >= minimumNext) {
                self.executeAutoBid(auctionId, saved.getId(), bidderId, minimumNext);
            }
        } else if (!isNew[0]) {
            // Leader updating maxBid: no immediate bid needed — they already hold
            // the top position.  Publish a synthetic BidEvent so onBidPlaced
            // re-evaluates competitors that may have been capped against the old
            // ceiling.  The event fires AFTER_COMMIT of this transaction.
            eventPublisher.publishEvent(new BidEvent(
                    auctionId,
                    auction.getTitle(),
                    auction.getSeller().getId(),
                    bidderId,
                    null,
                    auction.getCurrentPrice()
            ));
        }
        // else: fresh registration where bidder is already the leader — no action.

        return toResponse(saved);
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
     * Lắng nghe BidEvent sau khi transaction commit và giải quyết auto-bid
     * theo kiểu proxy/Vickrey trong một lần duy nhất (single-pass resolution for N users).
     *
     * <p>Dùng {@code AFTER_COMMIT} để đảm bảo đọc đúng giá hiện tại
     * từ transaction vừa commit trước đó.
     *
     * <p>Logic tính giá thắng:
     * <ul>
     *   <li><b>Case 3</b> – best.maxBid &lt; currentPrice + increment: deactivate, dừng.</li>
     *   <li><b>N-way tie</b> – N bidders share the same maxBid: earliest registrant wins
     *       at {@code currentPrice + minimumIncrement}; all tied peers are deactivated
     *       immediately in the same invocation — no cascade.</li>
     *   <li><b>Case 1</b> – chỉ một auto-bidder (không có second): đặt
     *       {@code currentPrice + minimumIncrement}.</li>
     *   <li><b>Case 2</b> – hai auto-bidder cạnh tranh: đặt
     *       {@code min(best.maxBid, second.maxBid + minimumIncrement)}.</li>
     *   <li><b>Cascade termination</b> – nếu best đã là người dẫn đầu: return ngay,
     *       không đặt bid mới. Đảm bảo cascade kết thúc sau tối đa 2 lần gọi.</li>
     * </ul>
     *
     * @param event thông tin bid vừa được chấp nhận
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBidPlaced(BidEvent event) {
        Auction auction = auctionRepository.findById(event.auctionId()).orElse(null);
        if (auction == null || !auction.isAcceptingBids()) return;

        String currentLeaderId = auction.getLeadingBidder() != null
                ? auction.getLeadingBidder().getId()
                : null;

        List<AutoBid> candidates = autoBidRepository.findByAuctionAndActiveTrue(auction);

        PriorityQueue<AutoBid> challengersQueue = new PriorityQueue<>(
                Comparator.comparingDouble(AutoBid::getMaxBid).reversed()
                        .thenComparing(BaseEntity::getCreationDate)
        );

        for (AutoBid ab : candidates) {
            boolean isLeader = ab.getBidder().getId().equals(currentLeaderId);
            if (!isLeader) {
                challengersQueue.offer(ab);
            }
        }

        if (challengersQueue.isEmpty()) return;

        double minimumNext = auction.getCurrentPrice() + auction.getMinimumIncrement();
        AutoBid bestChallenger = challengersQueue.peek();

        // Case 3: best challenger cannot even meet the minimum next bid — deactivate and stop.
        if (bestChallenger.getMaxBid() < minimumNext) {
            self.deactivateAutoBid(bestChallenger.getId());
            return;
        }

        // Now that we know at least one challenger can compete, compile the full queue
        // (including the current leader) to evaluate the win conditions.
        PriorityQueue<AutoBid> fullQueue = new PriorityQueue<>(
                Comparator.comparingDouble(AutoBid::getMaxBid).reversed()
                        .thenComparing(BaseEntity::getCreationDate)
        );
        for (AutoBid ab : candidates) {
            fullQueue.offer(ab);
        }

        AutoBid best = fullQueue.poll();

        // Drain N-way ties in one pass. The queue ordering (maxBid DESC, then
        // creationDate ASC) already guarantees best is the earliest registrant
        // among all peers at the same ceiling. Every other tied member is
        // deactivated immediately so the group collapses into a single outcome
        // here — no cascade across tied bidders across multiple invocations.
        while (!fullQueue.isEmpty()
                && Double.compare(fullQueue.peek().getMaxBid(), best.getMaxBid()) == 0) {
            self.deactivateAutoBid(fullQueue.poll().getId());
        }

        // Compute the single winning price in one pass (proxy/Vickrey-style).
        double winningPrice;
        AutoBid second = fullQueue.peek(); // first non-tied competitor, if any
        if (second == null) {
            // Case 1 (includes N-way tie with no outside competitor):
            // pay the minimum needed to take the lead.
            winningPrice = minimumNext;
        } else {
            // Case 2: winner pays just enough to beat the (non-tied) runner-up.
            winningPrice = Math.min(best.getMaxBid(), second.getMaxBid() + auction.getMinimumIncrement());
        }

        // Cascade-termination guard: if the computed winner is already the current
        // leader, they hold the position at the correct price — no new bid is needed.
        // This is what makes the cascade stop after at most 2 passes.
        if (best.getBidder().getId().equals(currentLeaderId)) {
            return;
        }

        self.executeAutoBid(event.auctionId(), best.getId(), best.getBidder().getId(), winningPrice);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeAutoBid(String auctionId, String autoBidId,
                               String bidderId, double amount) {
        // Re-validate eligibility in case bidder/seller status or balance
        // changed between registration and execution time.
        User bidder = userRepository.findById(bidderId).orElse(null);
        if (bidder == null || !bidder.isActive()) {
            self.deactivateAutoBid(autoBidId);
            return;
        }
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null || !auction.getSeller().isActive()) {
            self.deactivateAutoBid(autoBidId);
            return;
        }
        if (bidder.getBalance() < amount) {
            self.deactivateAutoBid(autoBidId);
            return;
        }
        try {
            bidService.placeBid(auctionId, bidderId,
                    new BidRequest(amount, "AUTO"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            self.deactivateAutoBid(autoBidId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateAutoBid(String autoBidId) {
        autoBidRepository.findById(autoBidId).ifPresent(ab -> {
            ab.deactivate();
            autoBidRepository.save(ab);
        });
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
