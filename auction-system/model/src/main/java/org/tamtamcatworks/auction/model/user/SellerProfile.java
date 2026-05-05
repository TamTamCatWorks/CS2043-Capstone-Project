package org.tamtamcatworks.auction.model.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hồ sơ Seller — lưu DB vĩnh viễn, liên kết 1-1 với User qua userId.
 *
 * KHÔNG kế thừa Entity — dùng chung userId làm PK với bảng users.
 *
 * Lưu lịch sử bán hàng và đánh giá uy tín lâu dài của người bán.
 * Khác SellerRole (RAM, tạm thời trong 1 phiên) —
 * SellerProfile tồn tại vĩnh viễn và tích lũy qua nhiều phiên.
 */
public class SellerProfile {

    private final String userId;

    // Danh sách auctionId các phiên đã tạo ra (mọi trạng thái)
    private final List<String> listings;

    // Điểm uy tín trung bình (1.0 – 5.0), tính từ đánh giá của Buyer
    private double rating;

    // Số lượt đánh giá — dùng để tính lại rating trung bình
    private int ratingCount;

    // Tổng doanh thu từ các phiên đã CLOSED thành công
    private double totalRevenue;

    // Tổng số phiên đã bán thành công (có người thắng)
    private int totalSold;

    // ── Constructor ───────────────────────────────────────────────────────────

    public SellerProfile(String userId) {
        this.userId       = userId;
        this.listings     = new ArrayList<>();
        this.rating       = 0.0;
        this.ratingCount  = 0;
        this.totalRevenue = 0.0;
        this.totalSold    = 0;
    }

    // ── Business methods ──────────────────────────────────────────────────────

    // Ghi nhận phiên mới tạo bởi Seller
    public void addListing(String auctionId) {
        if (!listings.contains(auctionId)) {
            listings.add(auctionId);
        }
    }

    // Ghi nhận bán thành công và cộng doanh thu
    public void recordSale(String auctionId, double salePrice) {
        totalSold++;
        totalRevenue += salePrice;
    }

    // Thêm đánh giá mới và tính lại trung bình cộng dồn
    public void addRating(double newRating) {
        if (newRating < 1.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating phải từ 1.0 đến 5.0.");
        }
        this.rating = (this.rating * this.ratingCount + newRating) / (this.ratingCount + 1);
        this.ratingCount++;
    }

    @Override
    public String toString() {
        return "SellerProfile{userId='" + userId
                + "', rating=" + String.format("%.1f", rating)
                + " (" + ratingCount + " đánh giá)"
                + ", sold=" + totalSold
                + ", revenue=" + String.format("%,.0f", totalRevenue) + " VNĐ}";
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getUserId()               { return userId; }
    public List<String> getListings()       { return Collections.unmodifiableList(listings); }
    public double getRating()               { return rating; }
    public int getRatingCount()             { return ratingCount; }
    public double getTotalRevenue()         { return totalRevenue; }
    public int getTotalSold()               { return totalSold; }

    public void setListings(List<String> listings) {
        this.listings.clear();
        this.listings.addAll(listings);
    }

    public void setRating(double rating)            { this.rating = rating; }
    public void setRatingCount(int ratingCount)     { this.ratingCount = ratingCount; }
    public void setTotalRevenue(double revenue)     { this.totalRevenue = revenue; }
    public void setTotalSold(int totalSold)         { this.totalSold = totalSold; }
}