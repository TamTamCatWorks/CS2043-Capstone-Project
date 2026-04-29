package org.tamtamcatworks.auction.model;

import org.tamtamcatworks.auction.observer.AuctionEvent;
import org.tamtamcatworks.auction.observer.AuctionEventType;
import org.tamtamcatworks.auction.observer.AuctionObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Phiên đấu giá — Subject trong Observer Pattern.
 *
 * <p>INHERITANCE (Sự kế thừa):
 * - Auction kế thừa Entity → có sẵn entityId, createdAt, getDisplayInfo()
 * - Implement getDisplayInfo() để hiển thị thông tin phiên đấu giá
 *
 * <p>OBSERVER PATTERN (Mẫu Observer):
 * - Auction là Subject → giữ danh sách observers (BidderRole, SellerRole)
 * - Khi có sự kiện (bid mới, mở phiên, đóng phiên...) → notifyObservers()
 * - Observers nhận event realtime và cập nhật state của mình
 *
 * <p>THREAD SAFETY (An toàn luồng):
 * - Các method thay đổi state (open, recordBid, close...) đều synchronized
 * - Đảm bảo không có race condition khi nhiều bidder đặt giá cùng lúc
 * - observers list cũng được bảo vệ bằng synchronized
 *
 * <p>STATE MACHINE (Máy trạng thái):
 * - PENDING → ACTIVE → CLOSED (luồng bình thường)
 * - PENDING/ACTIVE → CANCELLED (có thể hủy bất kỳ lúc nào)
 * - Không thể chuyển từ CLOSED/CANCELLED sang trạng thái khác
 */
public class Auction extends Entity {


    private String title;

    private final String itemId;

    private final String sellerId;

    private final double startingPrice;

    private double currentPrice;

    /** ID của User đang dẫn đầu phiên.
     * null khi chưa có bid nào. */
    private String leadingBidderId;

    /** Tên hiển thị của bidder đang dẫn đầu.
     * Lưu snapshot tên tại thời điểm bid (trường hợp user đổi tên sau này). */
    private String leadingBidderName;

    private final LocalDateTime startTime;

    /** Thời điểm kết thúc phiên.
     * MUTABLE → có thể gia hạn (extendEndTime) khi có bid gần hết giờ. */
    private LocalDateTime endTime;

    private AuctionStatus status;

    /** Bước giá tối thiểu mỗi lần bid.
     * Mặc định 1,000 VNĐ, có thể thay đổi.
     * Tại sao? Để tránh spam bid với giá tăng quá ít. */
    private double minimumIncrement;

    /** Lịch sử tất cả các bid đã được chấp nhận.
     * FINAL list → không thể thay đổi, chỉ thêm mới.
     * Dùng cho audit trail và thống kê. */
    private final List<BidTransaction> bidHistory;

    /** Danh sách observers đang theo dõi phiên này.
     * Bao gồm BidderRole và SellerRole.
     * MUTABLE → thêm khi user join, xóa khi user leave. */
    private final List<AuctionObserver> observers;

    // ── Constructor ───────────────────────────────────────────────────────────

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
    public Auction(String title, String itemId, String sellerId,
                   double startingPrice,
                   LocalDateTime startTime, LocalDateTime endTime) {
        super();  // Gọi Entity() để sinh UUID và timestamp
        if (startingPrice <= 0)
            throw new IllegalArgumentException("Giá khởi điểm phải > 0.");
        if (!endTime.isAfter(startTime))
            throw new IllegalArgumentException("endTime phải sau startTime.");

        this.title            = title;
        this.itemId           = itemId;
        this.sellerId         = sellerId;
        this.startingPrice    = startingPrice;
        this.currentPrice     = startingPrice;  // Ban đầu = giá khởi điểm
        this.startTime        = startTime;
        this.endTime          = endTime;
        this.status           = AuctionStatus.PENDING;  // Mặc định là chờ mở
        this.minimumIncrement = 1_000;  // Bước giá mặc định
        this.bidHistory       = new ArrayList<>();
        this.observers        = new ArrayList<>();
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
    public String getDisplayInfo() {
        return "Auction: " + title
                + " | ID: " + getEntityId()
                + " | Trạng thái: " + status
                + " | Giá khởi: " + String.format("%,.0f VNĐ", startingPrice)
                + " | Giá hiện tại: " + String.format("%,.0f VNĐ", currentPrice)
                + " | Dẫn đầu: " + (leadingBidderName != null ? leadingBidderName : "—")
                + " | Tổng bid: " + bidHistory.size()
                + " | Kết thúc: " + endTime;
    }

    // ── Observer Pattern Methods ────────────────────────────────────────────────

    /**
     * Đăng ký observer để nhận event từ phiên đấu giá.
     *
     * <p>TẠI SAO SYNCHRONIZED:
     * - observers list có thể được truy cập từ nhiều thread
     * - Tránh ConcurrentModificationException khi add/remove cùng lúc
     *
     * <p>LOGIC:
     * - Chỉ thêm nếu observer chưa tồn tại (tránh duplicate)
     * - Khi user joinAsBidder/joinAsSeller → gọi method này
     *
     * @param obs observer muốn đăng ký (BidderRole hoặc SellerRole)
     */
    public synchronized void registerObserver(AuctionObserver obs) {
        if (!observers.contains(obs)) observers.add(obs);
    }

    /**
     * Hủy đăng ký observer.
     *
     * <p>TẠI SAO SYNCHRONIZED:
     * - Tương tự registerObserver, cần thread-safe
     *
     * <p>LOGIC:
     * - Khi user leaveAuction() → gọi method này
     * - Observer không còn nhận event sau khi remove
     *
     * @param obs observer muốn hủy đăng ký
     */
    public synchronized void removeObserver(AuctionObserver obs) {
        observers.remove(obs);
    }

    /**
     * Thông báo event cho tất cả observers.
     *
     * <p>TẠI SAO SYNCHRONIZED:
     * - Đảm bảo không có observer bị bỏ sót hoặc nhận event trùng lặp
     *
     * <p>LOGIC:
     * - Duyệt qua tất cả observers
     * - Gọi onAuctionEvent() cho từng observer
     * - Try-catch để một observer lỗi không ảnh hưởng observer khác
     *
     * <p>EVENT TYPES:
     * - AUCTION_STARTED: khi open()
     * - BID_PLACED: khi recordBid()
     * - AUCTION_CLOSED: khi close()
     * - AUCTION_CANCELLED: khi cancel()
     * - AUCTION_EXTENDED: khi extendEndTime()
     *
     * @param event event cần thông báo
     */
    public synchronized void notifyObservers(AuctionEvent event) {
        for (AuctionObserver obs : observers) {
            try { obs.onAuctionEvent(event); }
            catch (Exception e) {
                System.err.println("[WARN] Observer error: " + e.getMessage());
            }
        }
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
        notifyObservers(new AuctionEvent.Builder(AuctionEventType.AUCTION_STARTED, getEntityId())
                .message("Phiên '" + title + "' bắt đầu!")
                .data("startingPrice", startingPrice).build());
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
        leadingBidderId     = tx.getBidderId();
        leadingBidderName   = tx.getBidderName();
        notifyObservers(new AuctionEvent.Builder(AuctionEventType.BID_PLACED, getEntityId())
                .message(tx.getBidderName() + " đặt "
                        + String.format("%,.0f VNĐ", tx.getAmount()))
                .data("bidAmount",   tx.getAmount())
                .data("bidderId",    tx.getBidderId())
                .data("bidderName",  tx.getBidderName())
                .data("timestamp",   tx.getTimestamp().toString()).build());
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
        notifyObservers(new AuctionEvent.Builder(AuctionEventType.AUCTION_CLOSED, getEntityId())
                .message(leadingBidderName != null
                        ? "Người thắng: " + leadingBidderName
                        : "Không có bid nào.")
                .data("winnerId",    leadingBidderId)
                .data("winnerName",  leadingBidderName)
                .data("finalPrice",  currentPrice).build());
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
        notifyObservers(new AuctionEvent.Builder(AuctionEventType.AUCTION_CANCELLED, getEntityId())
                .message("Phiên bị hủy: " + reason)
                .data("reason", reason).build());
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
        notifyObservers(new AuctionEvent.Builder(AuctionEventType.AUCTION_EXTENDED, getEntityId())
                .message("Gia hạn thêm " + extraSeconds + " giây.")
                .data("newEndTime", endTime.toString()).build());
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

    public String getTitle()                { return title; }
    public String getItemId()               { return itemId; }
    public String getSellerId()             { return sellerId; }
    public double getStartingPrice()        { return startingPrice; }
    public double getCurrentPrice()         { return currentPrice; }
    public String getLeadingBidderId()      { return leadingBidderId; }
    public String getLeadingBidderName()    { return leadingBidderName; }
    public LocalDateTime getStartTime()     { return startTime; }
    public LocalDateTime getEndTime()       { return endTime; }
    public AuctionStatus getStatus()        { return status; }
    public double getMinimumIncrement()     { return minimumIncrement; }
    public int getBidCount()               { return bidHistory.size(); }
    public boolean isAcceptingBids()        { return status.isAcceptingBids(); }
    public boolean hasBids()               { return !bidHistory.isEmpty(); }

    /**
     * Lấy lịch sử bid (read-only).
     *
     * <p>TẠI SAO UNMODIFIABLE:
     * - bidHistory là append-only, không cho phép sửa từ bên ngoài
     * - Tránh client code thêm/xóa bid không qua recordBid()
     * - Đảm bảo integrity của lịch sử
     *
     * @return list read-only của các bid transaction
     */
    public List<BidTransaction> getBidHistory() {
        return Collections.unmodifiableList(bidHistory);
    }

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