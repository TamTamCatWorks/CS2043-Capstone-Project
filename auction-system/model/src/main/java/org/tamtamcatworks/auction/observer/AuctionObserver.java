package org.tamtamcatworks.auction.observer;

/**
 * Interface Observer trong Observer Pattern.
 *
 * <p>OBSERVER PATTERN (Mẫu Observer):
 * - Định nghĩa contract cho các object muốn nhận notification từ Subject (Auction)
 * - Auction giữ danh sách AuctionObserver và notify khi có sự kiện
 * - Các class implement: BidderRole, SellerRole
 *
 * <p>DECOUPLING (Tách biệt):
 * - Auction không cần biết chi tiết về observer
 * - Observer không cần biết chi tiết về Auction
 * - Chỉ giao tiếp qua AuctionEvent
 *
 * <p>POLYMORPHISM (Đa hình):
 * - Auction có thể xử lý mọi loại observer như AuctionObserver
 * - Mỗi observer implement onAuctionEvent() theo cách riêng
 * - BidderRole: cập nhật holdAmount khi bị outbid
 * - SellerRole: cập nhật status khi phiên kết thúc
 *
 * <p>WHY INTERFACE:
 * - Cho phép thêm observer mới trong tương lai (EmailNotifier, WebSocket...)
 * - Auction.registerObserver() chấp nhận mọi loại observer
 * - Test dễ dàng hơn (có thể mock observer)
 *
 * <p>IMPLEMENTATION NOTES:
 * - onAuctionEvent() nên nhanh, không blocking
 * - Nếu cần xử lý lâu → nên chạy async
 * - Nên try-catch để không crash observer khác
 */
public interface AuctionObserver {

    /**
     * Xử lý event từ Auction.
     *
     * <p>METHOD CONTRACT:
     * - Được gọi bởi Auction.notifyObservers()
     * - Nhận AuctionEvent chứa thông tin về sự kiện
     * - Implementer nên xử lý event và cập nhật state của mình
     *
     * <p>EVENT TYPES:
     * - AUCTION_STARTED: phiên bắt đầu
     * - BID_PLACED: có bid mới
     * - AUCTION_CLOSED: phiên kết thúc
     * - AUCTION_CANCELLED: phiên bị hủy
     * - AUCTION_EXTENDED: thời gian gia hạn
     * - AUCTION_WON: xác định người thắng
     *
     * <p>IMPLEMENTATION GUIDELINES:
     * - Nên dùng switch(event.getType()) để xử lý từng loại
     * - Nên try-catch để không crash observer khác
     * - Nên nhanh, không blocking (nếu cần xử lý lâu → async)
     *
     * <p>EXAMPLE (BidderRole):
     * <pre>
     * switch (event.getType()) {
     *     case BID_PLACED → {
     *         if (isLeading && !newLeaderId.equals(myId)) {
     *             // Bị outbid → hoàn tiền
     *             user.refundBalance(holdAmount);
     *             holdAmount = 0;
     *         }
     *     }
     * }
     * </pre>
     *
     * @param event event từ Auction
     */
    void onAuctionEvent(AuctionEvent event);
}
