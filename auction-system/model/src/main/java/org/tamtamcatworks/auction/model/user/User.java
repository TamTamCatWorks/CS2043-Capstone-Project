package org.tamtamcatworks.auction.model.user;

import org.tamtamcatworks.auction.model.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * Người dùng trong hệ thống đấu giá.
 *
 * <p>INHERITANCE (Sự kế thừa):
 * - User kế thừa Entity → có sẵn entityId, createdAt, getDisplayInfo()
 * - Implement getDisplayInfo() để hiển thị thông tin user
 *
 * <p>BALANCE MANAGEMENT (Quản lý số dư):
 * - balance là số tiền hiện có trong tài khoản
 * - Khi bid: balance giảm (đóng băng vào holdAmount)
 * - Khi outbid: balance được hoàn lại từ holdAmount
 * - Khi thắng: holdAmount chuyển cho seller, không hoàn lại
 *
 * <p>AUCTION ROLES (Các vai trò đấu giá):
 * - auctionRoles là Map lưu các role tạm thời của user
 * - Key: auctionId, Value: AuctionRole (BidderRole hoặc SellerRole)
 * - Role chỉ tồn trên RAM, tạo khi join, xóa khi leave
 *
 * <p>PROFILES (Hồ sơ):
 * - buyerProfile: lịch sử mua hàng lâu dài (lưu DB)
 * - sellerProfile: lịch sử bán hàng lâu dài (lưu DB)
 * - Khác với role tạm thời, profile tồn tại vĩnh viễn
 */
public class User extends Entity {

    // ── Fields ──────────────────────────────────────────────────────────────────

    /** Tên đăng nhập (unique).
     * Dùng để xác thực user. */
    private String username;

    /** Email của user (unique).
     * Dùng để khôi phục mật khẩu và thông báo. */
    private String email;

    /** Mật khẩu đã được hash (không lưu plain text).
     * Tại sao hash? Để bảo mật nếu DB bị leak. */
    private String passwordHash;

    /** Tên đầy đủ hiển thị.
     * Có thể thay đổi sau khi tạo tài khoản. */
    private String fullName;

    /** Số dư tài khoản hiện tại.
     * MUTABLE → thay đổi khi bid, outbid, thắng, nạp tiền... */
    private double balance;


    /** Hồ sơ mua hàng (lịch sử lâu dài).
     * Lưu tổng chi, tổng thắng, bidding history.
     * Tồn tại vĩnh viễn trong DB. */
    private BuyerProfile buyerProfile;

    /** Hồ sơ bán hàng (lịch sử lâu dài).
     * Lưu doanh thu, rating, số lượng đã bán.
     * Tồn tại vĩnh viễn trong DB. */
    private SellerProfile sellerProfile;

    // ── Constructor ─────────────────────────────────────────────────────────────

    /**
     * Tạo user mới.
     *
     * <p>LOGIC CONSTRUCTOR:
     * 1. Gọi super() → sinh UUID cho entityId, ghi createdAt
     * 2. Gán thông tin cơ bản (username, email, passwordHash, fullName)
     * 3. Khởi tạo balance với số tiền ban đầu
     * 4. Tạo BuyerProfile và SellerProfile rỗng cho user
     *
     * <p>NOTE:
     * - passwordHash phải được hash trước khi truyền vào (không lưu plain text)
     * - initialBalance có thể = 0 cho user mới
     *
     * @param username tên đăng nhập
     * @param email email
     * @param passwordHash mật khẩu đã hash
     * @param fullName tên đầy đủ
     * @param initialBalance số dư ban đầu
     */
    public User(String username, String email, String passwordHash, String fullName, double initialBalance) {
        super();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.balance = initialBalance;

        // Tạo profile rỗng cho user mới
        this.buyerProfile = new BuyerProfile(getEntityId());
        this.sellerProfile = new SellerProfile(getEntityId());
    }


    // ── Balance Management ─────────────────────────────────────────────────────

    /**
     * Trừ số dư tài khoản.
     *
     * <p>USE CASE:
     * - Khi bid được chấp nhận: trừ balance, đóng băng vào holdAmount
     * - Khi nạp tiền (ngược lại): dùng refundBalance hoặc set trực tiếp
     *
     * <p>NOTE:
     * - Method này KHÔNG validate balance >= amount
     * - Validation nên được gọi trước khi deduct
     *
     * @param amount số tiền muốn trừ
     */
    public void deductBalance(double amount) {
        this.balance -= amount;
    }

    /**
     * Hoàn tiền vào tài khoản.
     *
     * <p>USE CASE:
     * - Khi bị outbid: hoàn holdAmount cũ
     * - Khi phiên bị hủy: hoàn toàn bộ holdAmount
     * - Khi nạp tiền
     *
     * @param amount số tiền muốn hoàn
     */
    public void refundBalance(double amount) {
        this.balance += amount;
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public double getBalance() {
        return balance;
    }

    public BuyerProfile getBuyerProfile() {
        return buyerProfile;
    }

    public SellerProfile getSellerProfile() {
        return sellerProfile;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Hiển thị thông tin tóm tắt của user.
     *
     * <p>IMPLEMENT POLYMORPHISM:
     * - Entity yêu cầu subclass implement getDisplayInfo()
     * - User cung cấp format riêng cho user
     *
     * <p>FORMAT:
     * - Username, ID, Balance
     *
     * @return chuỗi mô tả user
     */
    @Override
    public String getDisplayInfo() {
        return "User: " + username + " | ID: " + getEntityId() + " | Balance: " + balance;
    }
}