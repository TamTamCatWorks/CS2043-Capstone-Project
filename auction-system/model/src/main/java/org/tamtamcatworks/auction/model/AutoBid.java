package org.tamtamcatworks.auction.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.tamtamcatworks.auction.model.user.User;

/**
 * Lưu cấu hình đấu giá tự động của một user cho một phiên đấu giá.
 *
 * <p>Khi có bid mới, {@code AutoBidService} sẽ dùng PriorityQueue để tìm
 * auto-bidder tốt nhất (maxBid cao nhất) và tự động đặt giá thay họ,
 * cho đến khi không còn auto-bidder nào có thể cạnh tranh.
 */
@Entity
@Table(
    name = "auto_bids",
    uniqueConstraints = @UniqueConstraint(columnNames = {"auction_id", "bidder_id"})
)
public class AutoBid extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    private double maxBid;

    private boolean active;

    protected AutoBid() {}

    /**
     * Tạo một auto-bid mới.
     *
     * @param auction   phiên đấu giá muốn auto-bid
     * @param bidder    người đặt auto-bid
     * @param maxBid    mức giá tối đa sẵn sàng trả
     */
    public AutoBid(Auction auction, User bidder, double maxBid) {
        super();
        if (maxBid <= 0) {
            throw new IllegalArgumentException("maxBid phải > 0.");
        }
        if (maxBid <= auction.getCurrentPrice()) {
            throw new IllegalArgumentException(
                "maxBid phải lớn hơn giá hiện tại: " + auction.getCurrentPrice());
        }
        this.auction = auction;
        this.bidder = bidder;
        this.maxBid = maxBid;
        this.active = true;
    }

    /**
     * Cập nhật cấu hình auto-bid (upsert).
     *
     * @param maxBid    mức giá tối đa mới
     */
    public void update(double maxBid) {
        if (maxBid <= 0) {
            throw new IllegalArgumentException("maxBid phải > 0.");
        }

        this.maxBid = maxBid;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Auction getAuction() {
        return auction;
    }

    public User getBidder() {
        return bidder;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public boolean isActive() {
        return active;
    }
}
