package org.tamtamcatworks.auction.observer;

/**
 * Enum định nghĩa các loại event có thể xảy ra trong phiên đấu giá.
 *
 * <p>ENUM PATTERN (Mẫu Enum):
 * - Type-safe: không thể gán giá trị sai (khác từ String constant)
 * - Singleton: mỗi enum constant chỉ có một instance
 * - Switch-friendly: có thể dùng trong switch statement
 *
 * <p>EVENT LIFECYCLE (Vòng đời event):
 * 1. AUCTION_STARTED → Phiên bắt đầu (khi Auction.open())
 * 2. BID_PLACED → Có bid mới (khi Auction.recordBid()) - có thể lặp nhiều lần
 * 3. AUCTION_EXTENDED → Gia hạn thời gian (khi Auction.extendEndTime()) - có thể lặp nhiều lần
 * 4. AUCTION_CLOSED → Phiên kết thúc (khi Auction.close())
 * 5. AUCTION_CANCELLED → Phiên bị hủy (khi Auction.cancel())
 * 6. AUCTION_WON → Xác định người thắng (khi close và có người thắng)
 *
 * <p>WHY ENUM:
 * - Compile-time checking: compiler sẽ bắt lỗi nếu gán sai type
 * - IDE autocomplete: dễ dàng chọn event type
 * - Readability: code rõ ràng hơn so với string constant
 * - Extensibility: dễ thêm event type mới trong tương lai
 */
public enum AuctionEventType {

    /**
     * Phiên đấu giá bắt đầu.
     *
     * <p>KHI XẢY RA:
     * - Khi Auction.open() được gọi
     * - Status chuyển từ PENDING → ACTIVE
     *
     * <p>DATA KÈM THEO:
     * - startingPrice: giá khởi điểm
     */
    AUCTION_STARTED,

    /**
     * Có bid mới được đặt.
     *
     * <p>KHI XẢY RA:
     * - Khi Auction.recordBid() được gọi
     * - Mỗi lần có bid accepted đều phát event này
     *
     * <p>DATA KÈM THEO:
     * - bidAmount: số tiền bid
     * - bidderId: ID người đặt
     * - bidderName: tên người đặt
     * - timestamp: thời điểm đặt
     */
    BID_PLACED,

    /**
     * Phiên đấu giá kết thúc.
     *
     * <p>KHI XẢY RA:
     * - Khi Auction.close() được gọi
     * - Status chuyển từ ACTIVE → CLOSED
     *
     * <p>DATA KÈM THEO:
     * - winnerId: ID người thắng (null nếu không có bid)
     * - winnerName: tên người thắng (null nếu không có bid)
     * - finalPrice: giá cuối cùng
     */
    AUCTION_CLOSED,

    /**
     * Phiên đấu giá bị hủy.
     *
     * <p>KHI XẢY RA:
     * - Khi Auction.cancel() được gọi
     * - Status chuyển từ PENDING/ACTIVE → CANCELLED
     *
     * <p>DATA KÈM THEO:
     * - reason: lý do hủy
     */
    AUCTION_CANCELLED,

    /**
     * Thời gian phiên được gia hạn.
     *
     * <p>KHI XẢY RA:
     * - Khi Auction.extendEndTime() được gọi
     * - Thường khi có bid gần hết giờ
     *
     * <p>DATA KÈM THEO:
     * - newEndTime: thời điểm kết thúc mới
     */
    AUCTION_EXTENDED,

    /**
     * Xác định người thắng phiên.
     *
     * <p>KHI XẢY RA:
     * - Khi phiên đóng và có người thắng
     * - Thường đi kèm với AUCTION_CLOSED
     *
     * <p>DATA KÈM THEO:
     * - winnerId: ID người thắng
     * - winnerName: tên người thắng
     * - finalPrice: giá thắng
     */
    AUCTION_WON
}
