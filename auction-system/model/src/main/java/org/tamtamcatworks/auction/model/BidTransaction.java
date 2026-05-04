package org.tamtamcatworks.auction.model;

import java.time.LocalDateTime;
import java.util.UUID;

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
public final class BidTransaction {

    /** Loại bid: thủ công hoặc tự động. */
    public enum BidType {MANUAL, AUTO}

    private final String id;
    private final String auctionId;
    private final String bidderId;
    private final String bidderName;
    private final double amount;
    private final LocalDateTime timestamp;
    private final BidType bidType;

    // ── Constructors ─────────────────────────────────────────────────────────────

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
    public BidTransaction(String auctionId, String bidderId,
                          String bidderName, double amount, BidType bidType) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền đặt giá phải > 0.");
        this.id = UUID.randomUUID().toString();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.bidType = bidType;
    }

    /**
     * Constructor load bid transaction từ database.
     *
     * <p>TẠI SAO CẦN CONSTRUCTOR NÀY:
     * - Khi load từ DB → đã có id, timestamp sẵn
     * - Không thể dùng constructor default vì sẽ sinh ID mới (sai)
     * - Cần khôi phục lại chính xác trạng thái lưu trong DB
     *
     * <p>LOGIC:
     * - Gán trực tiếp tất cả field từ tham số
     * - Không sinh mới, không validate (đã validate khi lưu DB)
     *
     * @param id UUID đã có từ database
     * @param auctionId ID phiên đấu giá
     * @param bidderId ID bidder
     * @param bidderName tên bidder
     * @param amount số tiền bid
     * @param timestamp thời điểm đặt
     * @param bidType loại bid
     */
    public BidTransaction(String id, String auctionId, String bidderId,
                          String bidderName, double amount,
                          LocalDateTime timestamp, BidType bidType) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = timestamp;
        this.bidType = bidType;
    }

    // ── Getters ──────────────────────────────────────────────────────────────────
    // Vì class là immutable, chỉ có getters, không có setters

    public String getId() {
        return id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public String getBidderName() {
        return bidderName;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BidType getBidType() {
        return bidType;
    }

    /**
     * Kiểm tra bid này có phải auto-bid không.
     *
     * @return true nếu bidType là AUTO, false nếu MANUAL
     */
    public boolean isAutoBid() {
        return bidType == BidType.AUTO;
    }

    /**
     * Chuỗi đại diện cho bid transaction (dùng cho debugging/logging).
     *
     * <p>FORMAT:
     * - BidTransaction{bidder='Tên', amount=Giá VNĐ, type=Loại}
     *
     * <p>TẠI SAO FORMAT NÀY:
     * - Ngắn gọn, chứa thông tin quan trọng nhất
     * - Dễ đọc cho log
     *
     * @return chuỗi mô tả bid transaction
     */
    @Override
    public String toString() {
        return String.format("BidTransaction{bidder='%s', amount=%,.0f VNĐ, type=%s}",
                bidderName, amount, bidType);
    }
}