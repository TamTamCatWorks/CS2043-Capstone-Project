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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto-bidding service (proxy/Vickrey-style).
 *
 * <h3>Resolution algorithm — single-pass for N users ({@link #onBidPlaced})</h3>
 * <ol>
 *   <li>After a bid is accepted and committed ({@code BidEvent}), this listener fires.</li>
 *   <li>Load <b>all</b> active auto-bids for the auction (including the current leader's)
 *       into a {@link PriorityQueue} ordered by {@code maxBid DESC},
 *       tiebreaker: {@code creationDate ASC} (earlier registration wins ties).</li>
 *   <li>Poll {@code best} = rank 1.</li>
 *   <li><b>Case 3</b>: if {@code best.maxBid < currentPrice + increment} →
 *       deactivate only {@code best} and stop. The next-best is intentionally left
 *       active so it gets a fresh chance when the next {@code BidEvent} fires.</li>
 *   <li><b>Cascade-termination guard</b>: if {@code best} is already the current leader,
 *       scan the remaining queue and deactivate any entry that can no longer meet
 *       {@code minimumNext}, then return without placing a new bid. This is what
 *       stops the cascade after at most two {@code onBidPlaced} invocations.</li>
 *   <li><b>Tie drain</b>: deactivate all peers that share the same {@code maxBid} as
 *       {@code best} in a single pass — no cascade across tied bidders.</li>
 *   <li>Compute {@code winningPrice}:
 *     <ul>
 *       <li>No remaining competitor → {@code currentPrice + increment} (Case 1).</li>
 *       <li>Competitor present → {@code min(best.maxBid, second.maxBid + increment)} (Case 2).</li>
 *     </ul>
 *   </li>
 *   <li>Call {@link #executeAutoBid} for {@code best} at {@code winningPrice}.</li>
 * </ol>
 *
 * <h3>Auto-bid policy</h3>
 * Each user may hold at most one active auto-bid per auction (upsert semantics).
 */
@Service
public class AutoBidService {

    private static final Logger log = LoggerFactory.getLogger(AutoBidService.class);

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

    // ── Shared priority-queue comparator ─────────────────────────────────────
    // maxBid DESC (highest ceiling first), then creationDate ASC (earliest wins ties).
    private static final Comparator<AutoBid> AUTO_BID_ORDER =
            Comparator.comparingDouble(AutoBid::getMaxBid).reversed()
                      .thenComparing(BaseEntity::getCreationDate);

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers or updates an auto-bid for a given auction (upsert).
     *
     * <p>Dispatch rules after save:
     * <ul>
     *   <li><b>Non-leader (new or updated)</b>: if {@code maxBid >= minimumNext}, place
     *       an immediate bid at {@code minimumNext} via {@link #executeAutoBid}. The
     *       resulting {@code BidEvent} will trigger {@link #onBidPlaced}, which resolves
     *       any competing auto-bids in a single pass.</li>
     *   <li><b>Leader updating maxBid</b>: no immediate bid needed — they already hold
     *       the top position. A synthetic {@code BidEvent} is published so
     *       {@link #onBidPlaced} re-evaluates competitors that may have been capped
     *       against the old ceiling.</li>
     *   <li><b>Fresh registration by the current leader</b>: no action required.</li>
     * </ul>
     *
     * @param auctionId ID of the auction
     * @param bidderId  ID of the registering user
     * @param request   contains the declared {@code maxBid}
     * @return the saved auto-bid response
     */
    @Transactional
    public AutoBidResponse register(String auctionId, String bidderId, AutoBidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("Auction not found."));
        User bidder = userRepository.findById(bidderId)
            .orElseThrow(() -> new NoSuchElementException("Bidder not found."));

        // ── Eligibility guards (same order as BidService.placeBid) ───────────
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
                "maxBid must be higher than current price: " + auction.getCurrentPrice());
        }
        if (bidder.getId().equals(auction.getSeller().getId())) {
            throw new IllegalStateException("Seller can't place bids on your own auction.");
        }

        // ── Upsert ───────────────────────────────────────────────────────────
        // Track which path was taken so the post-save dispatch can differ.
        AutoBid autoBid = autoBidRepository.findByAuctionAndBidder(auction, bidder)
            .map(existing -> {
                existing.update(request.maxBid());
                return existing;
            })
            .orElseGet(() -> {
                return new AutoBid(auction, bidder, request.maxBid());
            });

        AutoBid saved = autoBidRepository.save(autoBid);

        // ── Post-save dispatch ────────────────────────────────────────────────
        String leaderId = auction.getLeadingBidder() != null
                ? auction.getLeadingBidder().getId()
                : null;
        double minimumNext = auction.getCurrentPrice() + auction.getMinimumIncrement();

        if (saved.getMaxBid() >= minimumNext) {
            // Publish AFTER_COMMIT so onBidPlaced reads the fully committed
            // auto-bid row for this bidder, enabling correct Vickrey resolution
            // against any existing competitors.
        }
        // else: fresh registration while already the leader — no action needed.
        eventPublisher.publishEvent(new BidEvent(
                auctionId,
                auction.getTitle(),
                auction.getSeller().getId(),
                bidderId,
                null,
                auction.getCurrentPrice()
        ));

        return toResponse(saved);
    }

    /**
     * Cancels a user's auto-bid on a given auction.
     *
     * @param auctionId ID of the auction
     * @param bidderId  ID of the user
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
     * Returns the current auto-bid for the given user and auction.
     *
     * @param auctionId ID of the auction
     * @param bidderId  ID of the user
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
            .orElseThrow(() -> new NoSuchElementException("No auto-bid found for this auction."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Event listener — core resolution logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Listens for {@link BidEvent} after transaction commit and resolves all active
     * auto-bids in a single pass (proxy/Vickrey-style).
     *
     * <p>{@code AFTER_COMMIT} ensures this reads the price committed by the triggering
     * transaction, not a stale snapshot. {@code REQUIRES_NEW} runs the resolution in
     * its own transaction so any deactivations or bids it causes are isolated.
     *
     * <h4>Resolution steps</h4>
     * <ol>
     *   <li>Build one {@link PriorityQueue} from all active auto-bids (including the
     *       current leader's), ordered {@code maxBid DESC, creationDate ASC}.</li>
     *   <li>Poll {@code best} (rank 1).
     *       <ul>
     *         <li><b>Case 3</b> – {@code best.maxBid < minimumNext}: deactivate only
     *             {@code best} and return. The next candidate is preserved for the
     *             next {@code BidEvent}.</li>
     *         <li><b>Cascade-termination guard</b> – if {@code best} is already the
     *             current leader: deactivate any remaining entries that can no longer
     *             meet {@code minimumNext}, then return without placing a new bid.</li>
     *       </ul>
     *   </li>
     *   <li><b>Tie drain</b>: deactivate every remaining entry that shares
     *       {@code best.maxBid} — collapses an N-way tie in one pass, no cascade.</li>
     *   <li>Compute {@code winningPrice}:
     *       <ul>
     *         <li><b>Case 1</b> (no remaining competitor): {@code minimumNext}.</li>
     *         <li><b>Case 2</b> (competitor present):
     *             {@code min(best.maxBid, second.maxBid + increment)}.</li>
     *       </ul>
     *   </li>
     *   <li>Call {@link #executeAutoBid} for {@code best} at {@code winningPrice}.</li>
     * </ol>
     *
     * @param event the accepted bid that triggered this resolution
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBidPlaced(BidEvent event) {
        Auction auction = auctionRepository.findById(event.auctionId()).orElse(null);
        if (auction == null || !auction.isAcceptingBids()) return;

        List<AutoBid> candidates = autoBidRepository.findByAuctionAndActiveTrue(auction);
        if (candidates.isEmpty()) return;

        String currentLeaderId = auction.getLeadingBidder() != null
                ? auction.getLeadingBidder().getId()
                : null;
        double minimumNext = auction.getCurrentPrice() + auction.getMinimumIncrement();

        // Build a single queue containing all active auto-bids, including the leader's.
        // Using one queue avoids the redundant double-rebuild in the original code.
        PriorityQueue<AutoBid> queue = new PriorityQueue<>(AUTO_BID_ORDER);
        queue.addAll(candidates);

        AutoBid best = queue.poll();
        if (best == null) return;

        // Case 3: the absolute best cannot even meet the minimum next price.
        // Deactivate only this entry and stop — the next-best stays active so
        // it gets its own chance on the following BidEvent.
        if (best.getMaxBid() < minimumNext) {
            self.deactivateAutoBid(best.getId());
            return;
        }

        // Cascade-termination guard: if the computed best is already the current
        // leader, only stop early when no remaining challenger can reach
        // minimumNext. Otherwise the leader must still auto-bid against the best
        // viable challenger.
        if (best.getBidder().getId().equals(currentLeaderId)) {
            deactivateBelowMinimumNext(queue, minimumNext);
            if (queue.isEmpty()) {
                return;
            }

            executeResolvedAutoBid(event, auction, best, queue.peek(), minimumNext);
            return;
        }

        // Tie drain: deactivate every peer that shares best's maxBid. The queue
        // ordering already guarantees best is the earliest registrant among all
        // tied entries, so every other member of the tied group loses cleanly in
        // this single invocation — no cascade across tied bidders.
        while (!queue.isEmpty()
                && Double.compare(queue.peek().getMaxBid(), best.getMaxBid()) == 0) {
            self.deactivateAutoBid(queue.poll().getId());
        }

        executeResolvedAutoBid(event, auction, best, queue.peek(), minimumNext);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transactional helpers — each runs in its own transaction so a failure in
    // one does not roll back the others.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates the auto-bidder's eligibility at execution time (status or balance
     * may have changed since registration), then delegates to {@link BidService}.
     * Deactivates the auto-bid on any failure so it does not linger.
     *
     * @param auctionId  ID of the auction
     * @param autoBidId  ID of the auto-bid being executed
     * @param bidderId   ID of the bidder
     * @param amount     the exact amount to bid
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeAutoBid(String auctionId, String autoBidId,
                               String bidderId, double amount) {
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
            bidService.placeBid(auctionId, bidderId, new BidRequest(amount, "AUTO"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            self.deactivateAutoBid(autoBidId);
        }
    }

    /**
     * Marks a single auto-bid as inactive and persists the change.
     *
     * @param autoBidId ID of the auto-bid to deactivate
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateAutoBid(String autoBidId) {
        autoBidRepository.findById(autoBidId).ifPresent(ab -> {
            ab.deactivate();
            autoBidRepository.save(ab);
        });
    }

    private void deactivateBelowMinimumNext(PriorityQueue<AutoBid> queue, double minimumNext) {
        while (!queue.isEmpty() && queue.peek().getMaxBid() < minimumNext) {
            self.deactivateAutoBid(queue.poll().getId());
        }
    }

    private void executeResolvedAutoBid(BidEvent event, Auction auction, AutoBid best,
                                        AutoBid second, double minimumNext) {
        double winningPrice = second == null
                ? minimumNext
                : Math.min(best.getMaxBid(), second.getMaxBid() + auction.getMinimumIncrement());
        self.executeAutoBid(event.auctionId(), best.getId(), best.getBidder().getId(), winningPrice);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private AutoBidResponse toResponse(AutoBid ab) {
        return new AutoBidResponse(
                ab.getId(),
                ab.getAuction().getId(),
                ab.getBidder().getId(),
                ab.getMaxBid(),
                ab.getAuction().getMinimumIncrement(),  // derived, not stored on AutoBid
                ab.isActive()
        );
    }
}