package org.tamtamcatworks.auction.model.user;

import org.tamtamcatworks.auction.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;


@Entity
@Table(name = "Users")
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private double balance;

    @Column(name = "hold_balance", nullable = false)
    private double holdBalance;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "buyer_profile_id")
    private BuyerProfile buyerProfile;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "seller_profile_id")
    private SellerProfile sellerProfile;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "admin_profile_id")
    private AdminProfile adminProfile;


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
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.balance = initialBalance;
        this.holdBalance = 0.0;
    }

    protected User() {} // for JPA


    // ── Balance Management ─────────────────────────────────────────────────────

    /**
     * Trừ số dư khả dụng.
     *
     * @param amount số tiền muốn trừ
     */
    public void deductBalance(double amount) {
        validatePositiveAmount(amount);
        if (balance < amount) {
            throw new IllegalArgumentException("Insufficient available balance.");
        }
        this.balance -= amount;
    }

    /**
     * Nạp tiền vào tài khoản.
     *
     * @param amount số tiền muốn nạp
     */
    public void addBalance(double amount) {
        validatePositiveAmount(amount);
        this.balance += amount;
    }

    /**
     * Hoàn tiền vào số dư khả dụng.
     *
     * @param amount số tiền muốn hoàn
     */
    public void refundBalance(double amount) {
        validatePositiveAmount(amount);
        this.balance += amount;
    }

    /**
     * Chuyển tiền từ số dư khả dụng sang số dư đang giữ cho bidding.
     *
     * @param amount số tiền muốn giữ
     */
    public void holdFunds(double amount) {
        validatePositiveAmount(amount);
        if (balance < amount) {
            throw new IllegalArgumentException("Insufficient available balance.");
        }
        balance -= amount;
        holdBalance += amount;
    }

    /**
     * Hoàn tiền đang bị giữ trở lại số dư khả dụng.
     *
     * @param amount số tiền muốn giải phóng
     */
    public void releaseHeldFunds(double amount) {
        validatePositiveAmount(amount);
        if (holdBalance < amount) {
            throw new IllegalArgumentException("Insufficient held balance.");
        }
        holdBalance -= amount;
        balance += amount;
    }

    /**
     * Tiêu hao tiền đang bị giữ khi bid thắng được chốt.
     *
     * @param amount số tiền muốn chốt
     */
    public void consumeHeldFunds(double amount) {
        validatePositiveAmount(amount);
        if (holdBalance < amount) {
            throw new IllegalArgumentException("Insufficient held balance.");
        }
        holdBalance -= amount;
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public double getBalance() {
        return balance;
    }

    public double getHoldBalance() {
        return holdBalance;
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

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public AdminProfile getAdminProfile() {
        return adminProfile;
    }

    public void setAdminProfile(AdminProfile adminProfile) {
        this.adminProfile = adminProfile;
    }

    public void setBuyerProfile(BuyerProfile buyerProfile)   { this.buyerProfile = buyerProfile; }
    public void setSellerProfile(SellerProfile sellerProfile) { this.sellerProfile = sellerProfile; }

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
    public String toString() {
        return "User: " + username + " | ID: " + getId() + " | Balance: " + balance;
    }

    private void validatePositiveAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be strictly positive");
        }
    }
}