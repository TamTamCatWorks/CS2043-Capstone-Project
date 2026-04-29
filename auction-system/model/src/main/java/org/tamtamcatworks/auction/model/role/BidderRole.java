package org.tamtamcatworks.auction.model.role;

import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.observer.AuctionEvent;
import org.tamtamcatworks.auction.observer.AuctionObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Trạng thái tạm thời của User trong vai trò Bidder tại một phiên.
 *
 * Chỉ tồn tại trên RAM — tạo khi User.joinAsBidder(), xóa khi leaveAuction().
 * Lịch sử lâu dài (tổng chi, tổng thắng) được lưu vào BuyerProfile (DB).
 *
 * OBSERVER: implements AuctionObserver để nhận event realtime từ Auction.
 *
 * holdAmount — số tiền đang bị "đóng băng":
 *   Khi bid được chấp nhận: balance -= bidAmount, holdAmount = bidAmount
 *   Khi bị outbid: balance += holdAmount cũ, holdAmount = 0
 *   Khi thắng: holdAmount chuyển cho Seller, không hoàn lại
 *   Khi phiên hủy: holdAmount được hoàn đầy đủ
 */
public class BidderRole implements AuctionRole, AuctionObserver {

    private final User user;
    private final String auctionId;

    // Tiền đang bị giữ = giá bid hiện tại của Bidder này
    private double holdAmount;

    // Đang dẫn đầu phiên
    private boolean isLeading;

    // Đã thắng phiên
    private boolean hasWon;

    // Lịch sử các mức giá đã đặt trong phiên này — dùng cho BidChart
    private final List<Double> myBidAmounts;

    // Số lần đặt giá trong phiên này
    private int bidCount;

    // Giá tối đa sẵn sàng trả (auto-bid). null = tắt
    private Double maxAutoBid;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BidderRole(User user, String auctionId) {
        this.user          = user;
        this.auctionId     = auctionId;
        this.holdAmount    = 0.0;
        this.isLeading     = false;
        this.hasWon        = false;
        this.myBidAmounts  = new ArrayList<>();
        this.bidCount      = 0;
        this.maxAutoBid    = null;
    }

    // ── AuctionRole ───────────────────────────────────────────────────────────

    @Override public String getRoleName()  { return "BIDDER"; }
    @Override public String getAuctionId() { return auctionId; }
    @Override public String getUserId()    { return user.getEntityId(); }

    // ── Observer ──────────────────────────────────────────────────────────────

    @Override
    public void onAuctionEvent(AuctionEvent event) {
        switch (event.getType()) {

            case BID_PLACED -> {
                // Có người bid mới — kiểm tra mình có bị outbid không
                String newLeaderId = event.getData("bidderId", String.class);
                if (isLeading && newLeaderId != null
                        && !newLeaderId.equals(user.getEntityId())) {
                    // Bị vượt — hoàn tiền holdAmount
                    user.refundBalance(holdAmount);
                    holdAmount = 0.0;
                    isLeading  = false;
                    System.out.println("[OUTBID] " + user.getUsername()
                            + " bị vượt trong phiên " + auctionId);
                }
            }

            case AUCTION_WON -> {
                hasWon    = true;
                isLeading = false;
                Double price = event.getData("finalPrice", Double.class);
                System.out.println("[WON] " + user.getUsername()
                        + " thắng phiên " + auctionId
                        + " — " + String.format("%,.0f VNĐ", price));
            }

            case AUCTION_CANCELLED -> {
                // Phiên bị hủy → hoàn toàn bộ holdAmount
                if (holdAmount > 0) {
                    user.refundBalance(holdAmount);
                    holdAmount = 0.0;
                }
                isLeading = false;
            }

            case AUCTION_CLOSED -> {
                isLeading = false;
            }

            default -> {
                System.out.println("[NOTIFY → " + user.getUsername() + "] "
                        + event.getType() + ": " + event.getMessage());
            }
        }
    }

    // ── Business ──────────────────────────────────────────────────────────────

    /**
     * Ghi nhận bid được BidProcessor chấp nhận.
     * Thứ tự: hoàn holdAmount cũ → trừ bidAmount mới → cập nhật state.
     */
    public void onBidAccepted(double bidAmount) {
        if (holdAmount > 0) {
            user.refundBalance(holdAmount); // hoàn bid cũ của chính mình
        }
        user.deductBalance(bidAmount);
        holdAmount = bidAmount;
        isLeading  = true;
        bidCount++;
        myBidAmounts.add(bidAmount);
    }

    public void enableAutoBid(double maxAmount) {
        if (maxAmount <= 0)
            throw new IllegalArgumentException("maxAutoBid phải > 0.");
        this.maxAutoBid = maxAmount;
    }

    public void disableAutoBid() { this.maxAutoBid = null; }

    // ── Getters ───────────────────────────────────────────────────────────────

    public User getUser()                   { return user; }
    public double getHoldAmount()           { return holdAmount; }
    public boolean isLeading()              { return isLeading; }
    public boolean isHasWon()              { return hasWon; }
    public int getBidCount()               { return bidCount; }
    public Double getMaxAutoBid()          { return maxAutoBid; }
    public boolean isAutoBidEnabled()      { return maxAutoBid != null; }

    public List<Double> getMyBidAmounts() {
        return Collections.unmodifiableList(myBidAmounts);
    }

    public double getLastBidAmount() {
        return myBidAmounts.isEmpty() ? 0.0
                : myBidAmounts.get(myBidAmounts.size() - 1);
    }
}