package org.tamtamcatworks.auction.model.role;

/**
 * Interface hợp đồng cho mọi role trong phiên đấu giá.
 *
 * <p>INTERFACE PATTERN (Mẫu Interface):
 * - Định nghĩa contract chung cho tất cả các role
 * - BidderRole và SellerRole đều implement interface này
 * - Cho phép xử lý polymorphic: AuctionRole role = new BidderRole(...)
 *
 * <p>TEMPORARY OBJECT (Đối tượng tạm thời):
 * - Role chỉ tồn tại trên RAM trong thời gian phiên chạy
 * - Không lưu vào database (không cần persist)
 * - Bị giải phóng khi User.leaveAuction() được gọi
 *
 * <p>SEPARATION OF CONCERNS (Tách biệt mối quan tâm):
 * - User là entity vĩnh viễn (lưu DB)
 * - AuctionRole là state tạm thời (chỉ RAM)
 * - BuyerProfile/SellerProfile là lịch sử lâu dài (lưu DB)
 * - BidderRole/SellerRole là state runtime (chỉ RAM)
 *
 * <p>WHY INTERFACE (Tại sao dùng interface):
 * - Cho phép thêm role mới trong tương lai (AdminRole, ModeratorRole...)
 * - Giúp Auction.registerObserver() chấp nhận mọi loại role
 * - Tăng tính mở rộng và linh hoạt
 */
public interface AuctionRole {

    /**
     * Lấy tên role.
     *
     * <p>RETURN VALUES:
     * - "BIDDER" cho BidderRole
     * - "SELLER" cho SellerRole
     * - Có thể mở rộng thêm trong tương lai
     *
     * @return tên role dạng chuỗi
     */
    String getRoleName();

    /**
     * Lấy ID của phiên đấu giá mà role này tham gia.
     *
     * <p>PURPOSE:
     * - Xác định phiên đấu giá mà user đang tham gia
     * - Dùng để map role với Auction tương ứng
     *
     * @return auctionId của phiên đấu giá
     */
    String getAuctionId();

    /**
     * Lấy ID của User sở hữu role này.
     *
     * <p>PURPOSE:
     * - Xác định user nào đang giữ role này
     * - Dùng để kiểm tra permission và ownership
     *
     * @return userId của user sở hữu role
     */
    String getUserId();
}