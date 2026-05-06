package org.tamtamcatworks.auction.model;

import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name="auctions")
public class Auction extends BaseEntity {

    private String title;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private double startingPrice;
    private double currentPrice;
    private double minimumIncrement;

    @ManyToOne
    @JoinColumn(name = "leading_bidder_id")
    private User leadingBidder;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<BidTransaction> bidHistory = new ArrayList<>();

    // optimistic locking
    @Version
    private Long version; 



    // ── Constructor ───────────────────────────────────────────────────────────

    protected Auction() {};
    /**
     * Tạo phiên đấu giá mới.
     *
     * <p>LOGIC CONSTRUCTOR:
     * 1. Gọi super() → sinh UUID cho entityId, ghi createdAt
     * 2. Validate: startingPrice > 0, endTime > startTime
     * 3. Khởi tạo state ban đầu:
     *    - currentPrice = startingPrice
     *    - status = PENDING (chưa mở)
     *    - minimumIncrement = 1,000 VNĐ (mặc định)
     *    - leadingBidder = null (chưa có bid)
     * 4. Tạo empty lists cho bidHistory và observers
     *
     * <p>VALIDATION RULES:
     * - startingPrice phải > 0 (không đấu giá miễn phí)
     * - endTime phải sau startTime (phiên phải có thời lượng)
     *
     * @param title tiêu đề phiên đấu giá
     * @param itemId ID của item đang đấu giá
     * @param sellerId ID của người bán
     * @param startingPrice giá khởi điểm (phải > 0)
     * @param startTime thời điểm bắt đầu
     * @param endTime thời điểm kết thúc (phải sau startTime)
     * @throws IllegalArgumentException nếu startingPrice <= 0 hoặc endTime <= startTime
     */
    public Auction(String title, User seller, Item item,
                   double startingPrice,
                   LocalDateTime startTime, LocalDateTime endTime) {
        super();  // Gọi Entity() để sinh UUID và timestamp
        if (startingPrice <= 0)
            throw new IllegalArgumentException("Giá khởi điểm phải > 0.");
        if (!endTime.isAfter(startTime))
            throw new IllegalArgumentException("endTime phải sau startTime.");

        this.title            = title;
        this.seller           = seller;
        this.item             = item;
        this.startingPrice    = startingPrice;
        this.currentPrice     = startingPrice;  // Ban đầu = giá khởi điểm
        this.startTime        = startTime;
        this.endTime          = endTime;
        this.status           = AuctionStatus.PENDING;  // Mặc định là chờ mở
        this.minimumIncrement = 1_000;  // Bước giá mặc định
    }

    // ── Override getDisplayInfo ────────────────────────────────────────────────

    /**
     * Hiển thị thông tin tóm tắt của phiên đấu giá.
     *
     * <p>IMPLEMENT POLYMORPHISM:
     * - Entity yêu cầu subclass implement getDisplayInfo()
     * - Auction cung cấp format riêng cho phiên đấu giá
     *
     * <p>FORMAT HIỂN THỊ:
     * - Tiêu đề, ID, Trạng thái
     * - Giá khởi điểm và giá hiện tại
     * - Người đang dẫn đầu (hoặc — nếu chưa có)
     * - Tổng số bid đã đặt
     * - Thời điểm kết thúc
     *
     * @return chuỗi mô tả phiên đấu giá
     */
    @Override
    public String toString() {
        return "Auction: " + title
                + " | ID: " + getId()
                + " | Trạng thái: " + status
                + " | Giá khởi: " + String.format("%,.0f VNĐ", startingPrice)
                + " | Giá hiện tại: " + String.format("%,.0f VNĐ", currentPrice)
                + " | Dẫn đầu: " + (leadingBidder != null ? leadingBidder.getFullName() : "—")
                // + " | Tổng bid: " + bidHistory.size()
                + " | Kết thúc: " + endTime;
    }



    // ── Business Logic Methods ─────────────────────────────────────────────────

    /**
     * Mở phiên đấu giá để bắt đầu nhận bid.
     *
     * <p>PRECONDITION:
     * - status phải là PENDING
     *
     * <p>LOGIC:
     * 1. Validate state: chỉ mở được từ PENDING
     * 2. Thay đổi status → ACTIVE
     * 3. Notify observers với event AUCTION_STARTED
     *
     * <p>TẠI SAO SYNCHRONIZED:
     * - Tránh race condition: nhiều thread gọi open() cùng lúc
     * - Đảm bảo state transition là atomic
     *
     * @throws IllegalStateException nếu status không phải PENDING
     */
    public synchronized void open() {
        if (status != AuctionStatus.PENDING)
            throw new IllegalStateException("Chỉ mở được phiên PENDING.");
        status = AuctionStatus.ACTIVE;
    }

    /**
     * Ghi nhận bid đã được chấp nhận bởi BidProcessor.
     *
     * <p>LOGIC:
     * 1. Thêm bid vào bidHistory (append-only)
     * 2. Cập nhật currentPrice = bid amount
     * 3. Cập nhật leadingBidder = bidder của bid này
     * 4. Notify observers với event BID_PLACED
     *
     * <p>TẠI SAO SYNCHRONIZED:
     * - Tránh race condition: nhiều bidder đặt giá cùng lúc
     * - Đảm bảo bidHistory và currentPrice luôn nhất quán
     *
     * <p>NOTE:
     * - Method này KHÔNG validate bid amount (đã validate ở BidProcessor)
     * - Chỉ ghi nhận bid đã được chấp nhận
     *
     * @param tx transaction bid đã được validate
     */
    public synchronized void recordBid(BidTransaction tx) {
        bidHistory.add(tx);
        currentPrice        = tx.getAmount();
        leadingBidder     = tx.getBidder();
    }

    /**
     * Đóng phiên đấu giá theo thời gian.
     *
     * <p>PRECONDITION:
     * - status phải là ACTIVE
     *
     * <p>LOGIC:
     * 1. Validate state: chỉ đóng được từ ACTIVE
     * 2. Thay đổi status → CLOSED
     * 3. Notify observers với event AUCTION_CLOSED
     * 4. Event chứa thông tin người thắng (hoặc null nếu không có bid)
     *
     * <p>TẠI SAO SYNCHRONIZED:
     * - Tránh race condition: timer và manual close cùng lúc
     *
     * @throws IllegalStateException nếu status không phải ACTIVE
     */
    public synchronized void close() {
        if (status != AuctionStatus.ACTIVE)
            throw new IllegalStateException("Chỉ đóng được phiên ACTIVE.");
        status = AuctionStatus.CLOSED;
    }

    /**
     * Hủy phiên đấu giá (bởi Admin hoặc Seller).
     *
     * <p>PRECONDITION:
     * - status KHÔNG được là CLOSED hoặc CANCELLED
     *
     * <p>LOGIC:
     * 1. Validate state: chỉ hủy được khi chưa kết thúc
     * 2. Thay đổi status → CANCELLED
     * 3. Notify observers với event AUCTION_CANCELLED
     * 4. Event chứa lý do hủy
     *
     * <p>HỒU QUẢ:
     * - Tất cả holdAmount sẽ được hoàn lại cho bidder
     * - Không có người thắng, item không được bán
     *
     * @param reason lý do hủy phiên
     * @throws IllegalStateException nếu phiên đã kết thúc
     */
    public synchronized void cancel(String reason) {
        if (status.isFinished())
            throw new IllegalStateException("Phiên đã kết thúc rồi.");
        status = AuctionStatus.CANCELLED;
    }

    /**
     * Gia hạn thời gian kết thúc phiên.
     *
     * <p>LOGIC:
     * 1. Cộng thêm seconds vào endTime
     * 2. Notify observers với event AUCTION_EXTENDED
     *
     * <p>TẠI SAO CẦN:
     * - Khi có bid gần hết giờ → gia hạn để bidder khác có cơ hội
     * - Tăng tính cạnh tranh và giá bán
     *
     * <p>TẠI SAO SYNCHRONIZED:
     * - Tránh race condition khi nhiều bid gần hết giờ
     *
     * @param extraSeconds số giây muốn gia hạn
     */
    public synchronized void extendEndTime(int extraSeconds) {
        endTime = endTime.plusSeconds(extraSeconds);
    }

    /**
     * Kiểm tra một mức giá có hợp lệ để bid không.
     *
     * <p>VALIDATION RULES:
     * 1. amount phải > currentPrice
     * 2. (amount - currentPrice) phải >= minimumIncrement
     *
     * <p>TẠI SAO CẦN RULES:
     * - Rule 1: Bid phải cao hơn giá hiện tại
     * - Rule 2: Tránh spam bid với giá tăng quá ít (minimumIncrement)
     *
     * <p>NOTE:
     * - Method này KHÔNG synchronized vì chỉ đọc state
     * - Gọi trước khi recordBid() để validate
     *
     * @param amount mức giá muốn bid
     * @return true nếu hợp lệ, false nếu không
     */
    public boolean isValidBidAmount(double amount) {
        return amount > currentPrice && (amount - currentPrice) >= minimumIncrement;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public List<BidTransaction> getBidHistory() {
        return Collections.unmodifiableList(bidHistory);
    }

    public String getTitle()                { return title; }
    public User getSeller()                 { return seller; }
    public Item getItem()                   { return item; }
    public double getStartingPrice()        { return startingPrice; }
    public double getCurrentPrice()         { return currentPrice; }
    public User getLeadingBidder()          { return leadingBidder; }
    public LocalDateTime getStartTime()     { return startTime; }
    public LocalDateTime getEndTime()       { return endTime; }
    public AuctionStatus getStatus()        { return status; }
    public double getMinimumIncrement()     { return minimumIncrement; }
    public boolean isAcceptingBids()        { return status.isAcceptingBids(); }

    /**
     * Cập nhật tiêu đề phiên đấu giá.
     *
     * <p>NOTE:
     * - Chỉ nên sửa khi phiên chưa mở (PENDING)
     * - Sửa khi đang chạy có thể gây confusion cho bidder
     *
     * @param title tiêu đề mới
     */
    public void setTitle(String title)              { this.title = title; }

    /**
     * Cập nhật bước giá tối thiểu.
     *
     * <p>VALIDATION:
     * - minimumIncrement phải >= 0
     *
     * <p>NOTE:
     * - Chỉ nên sửa khi phiên chưa mở
     * - Sửa khi đang chạy có thể ảnh hưởng bidder đang chờ
     *
     * @param inc bước giá mới
     * @throws IllegalArgumentException nếu inc < 0
     */
    public void setMinimumIncrement(double inc) {
        if (inc < 0) throw new IllegalArgumentException("Bước giá không được âm.");
        this.minimumIncrement = inc;
    }
}