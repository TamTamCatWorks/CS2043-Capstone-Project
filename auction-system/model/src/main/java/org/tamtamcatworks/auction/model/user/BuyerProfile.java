package org.tamtamcatworks.auction.model.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hồ sơ Buyer — lưu DB vĩnh viễn, liên kết 1-1 với User qua userId.
 *
 * KHÔNG kế thừa Entity vì không tự sinh id riêng.
 * Primary Key trong DB là userId — dùng chung với bảng users (1-1).
 *
 * Lưu lịch sử đặt giá và danh sách phiên đang theo dõi.
 * Dữ liệu ở đây là lịch sử LÂU DÀI — khác với BidderRole
 * chỉ tồn tại trên RAM trong thời gian 1 phiên.
 */
public class BuyerProfile {

    // userId của User sở hữu profile này — FK trong DB
    private final String userId;

    // Danh sách auctionId các phiên đã tham gia đặt giá (kể cả thua)
    private final List<String> biddingHistory;

    // Danh sách auctionId đang theo dõi để nhận thông báo
    private final List<String> watchlist;

    // Tổng số phiên đã thắng
    private int totalWins;

    // Tổng tiền đã chi mua hàng thành công
    private double totalSpent;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BuyerProfile(String userId) {
        this.userId         = userId;
        this.biddingHistory = new ArrayList<>();
        this.watchlist      = new ArrayList<>();
        this.totalWins      = 0;
        this.totalSpent     = 0.0;
    }

    // ── Business methods ──────────────────────────────────────────────────────

    // Ghi nhận tham gia phiên đấu giá (dù thắng hay thua)
    public void addToBiddingHistory(String auctionId) {
        if (!biddingHistory.contains(auctionId)) {
            biddingHistory.add(auctionId);
        }
    }

    // Thêm phiên vào watchlist
    public void addToWatchlist(String auctionId) {
        if (!watchlist.contains(auctionId)) {
            watchlist.add(auctionId);
        }
    }

    // Xóa phiên khỏi watchlist (khi phiên kết thúc hoặc user bỏ theo dõi)
    public void removeFromWatchlist(String auctionId) {
        watchlist.remove(auctionId);
    }

    // Ghi nhận thắng phiên và cộng vào tổng tiền chi
    public void recordWin(String auctionId, double finalPrice) {
        totalWins++;
        totalSpent += finalPrice;
        removeFromWatchlist(auctionId);
    }

    @Override
    public String toString() {
        return "BuyerProfile{userId='" + userId
                + "', wins=" + totalWins
                + ", totalSpent=" + String.format("%,.0f", totalSpent) + " VNĐ}";
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getUserId()                   { return userId; }
    public List<String> getBiddingHistory()     { return Collections.unmodifiableList(biddingHistory); }
    public List<String> getWatchlist()          { return Collections.unmodifiableList(watchlist); }
    public int getTotalWins()                   { return totalWins; }
    public double getTotalSpent()               { return totalSpent; }

    public void setBiddingHistory(List<String> history) {
        this.biddingHistory.clear();
        this.biddingHistory.addAll(history);
    }

    public void setWatchlist(List<String> watchlist) {
        this.watchlist.clear();
        this.watchlist.addAll(watchlist);
    }

    public void setTotalWins(int totalWins)     { this.totalWins = totalWins; }
    public void setTotalSpent(double totalSpent){ this.totalSpent = totalSpent; }
}