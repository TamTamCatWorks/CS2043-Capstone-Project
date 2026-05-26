package org.tamtamcatworks.auction.model;

import jakarta.persistence.*;
import org.tamtamcatworks.auction.model.user.User;

/**
 * Bản ghi BẤT BIẾN (immutable) của một lần đặt giá.
 *
 * <p>IMMUTABLE CLASS (Lớp bất biến):
 * - Mọi field đều là final → không thể thay đổi sau khi tạo
 * - Class là final → không thể subclass để bypass immutability
 * - Tại sao? Để đảm bảo integrity của lịch sử bid (audit trail)
 *
 * <p>VALUE OBJECT (Giá trị):
 * - BidTransaction là value object, không phải entity
 * - Không có identity riêng ngoài các field của nó
 * - Hai BidTransaction bằng nhau nếu tất cả field bằng nhau
 *
 * <p>BID TYPE:
 * - MANUAL: Bidder đặt giá thủ công qua UI
 * - AUTO: Hệ thống tự động đặt giá (auto-bid feature)
 */

@Entity
@Table(name = "bid_transactions")
public class BidTransaction extends BaseEntity {

    /** Loại bid: thủ công hoặc tự động. */
    public enum BidType {MANUAL, AUTO}


    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    private double amount;

    @Enumerated(EnumType.STRING)
    private BidType bidType;

    /**
     * Tạo bid transaction mới.
     *
     * <p>LOGIC CONSTRUCTOR:
     * 1. Validate: amount phải > 0
     * 2. Sinh UUID cho id
     * 3. Ghi timestamp hiện tại
     * 4. Gán tất cả field (đều final)
     *
     * <p>VALIDATION:
     * - amount phải > 0 (không bid âm hoặc bằng 0)
     *
     * @param auctionId ID phiên đấu giá
     * @param bidderId ID bidder đặt giá
     * @param bidderName tên hiển thị bidder
     * @param amount số tiền bid (phải > 0)
     * @param bidType loại bid (MANUAL hoặc AUTO)
     * @throws IllegalArgumentException nếu amount <= 0
     */
    public BidTransaction(Auction auction, User bidder, double amount, BidType bidType) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền đặt giá phải > 0.");
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.bidType = bidType;
    }

    protected BidTransaction () { }

    // ── Getters ──────────────────────────────────────────────────────────────────
    // Vì class là immutable, chỉ có getters, không có setters

    public Auction getAuction() {
        return auction;
    }

    public User getBidder() {
        return bidder;
    }

    public double getAmount() {
        return amount;
    }

    public BidType getBidType() {
        return bidType;
    }

    public boolean isAutoBid() {
        return bidType == BidType.AUTO;
    }


    @Override
    public String toString() {
        String bidderName = (bidder == null || bidder.getFullName() == null) ? "N/A" : bidder.getFullName();
        String typeStr = (bidType == null) ? "N/A" : bidType.name();
        return String.format("BidTransaction{bidder='%s', amount=%,.0f VNĐ, type=%s}",
                bidderName, amount, typeStr);
    }
}