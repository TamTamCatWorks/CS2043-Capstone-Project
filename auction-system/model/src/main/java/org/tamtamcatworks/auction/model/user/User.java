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
    }

    protected User() {} // for JPA


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

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
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
}