package org.tamtamcatworks.auction.model;

/**
 * Trạng thái vòng đời của một phiên đấu giá.
 *
 * <p>Luồng chuyển trạng thái hợp lệ:
 * <pre>
 *   PENDING ──► ACTIVE ──► CLOSED
 *                  │
 *                  └──────► CANCELLED
 * </pre>
 *
 * <p>Giải thích từng trạng thái:
 * <ul>
 *   <li>PENDING   — Phiên đã được tạo nhưng chưa đến giờ bắt đầu</li>
 *   <li>ACTIVE    — Phiên đang diễn ra, bidder có thể đặt giá</li>
 *   <li>CLOSED    — Phiên kết thúc đúng hạn, đã xác định người thắng</li>
 *   <li>CANCELLED — Phiên bị Admin hoặc Seller hủy trước khi kết thúc</li>
 * </ul>
 */
public enum AuctionStatus {

    /** Đã tạo, chưa đến giờ mở. Không ai đặt giá được. */
    PENDING,

    /** Đang diễn ra. Bidder có thể đặt giá bất kỳ lúc nào. */
    ACTIVE,

    /** Đã kết thúc theo giờ định sẵn. Người dẫn đầu là người thắng. */
    CLOSED,

    /** Bị hủy. Không có người thắng, hoàn tiền nếu cần. */
    CANCELLED;

    /**
     * Kiểm tra phiên có đang nhận bid không.
     *
     * @return true chỉ khi trạng thái là ACTIVE
     */
    public boolean isAcceptingBids() {
        return this == ACTIVE;
    }

    /**
     * Kiểm tra phiên đã kết thúc (dù thắng hay bị hủy).
     *
     * @return true khi là CLOSED hoặc CANCELLED
     */
    public boolean isFinished() {
        return this == CLOSED || this == CANCELLED;
    }
}