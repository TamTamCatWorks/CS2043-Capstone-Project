package org.tamtamcatworks.auction.model.role;

import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.observer.AuctionEvent;
import org.tamtamcatworks.auction.observer.AuctionObserver;

/**
 * Trạng thái tạm thời của User trong vai trò Seller tại một phiên.
 *
 * Chỉ tồn tại trên RAM — tạo khi User.joinAsSeller(), xóa khi leaveAuction().
 * Kết quả lâu dài (doanh thu, tổng bán) lưu vào SellerProfile (DB).
 *
 * Seller KHÔNG được đặt giá trong phiên mình bán.
 * Kiểm tra: BidProcessor kiểm tra sellerId != bidderId trước khi xử lý bid.
 */
public class SellerRole implements AuctionRole, AuctionObserver {

    // Trạng thái phiên từ góc nhìn Seller
    public enum Status { WAITING, ONGOING, SOLD, UNSOLD, CANCELLED }

    private final User user;
    private final String auctionId;
    private final String itemId;     // sản phẩm đem ra đấu giá

    private String winnerId;         // null cho đến khi phiên kết thúc
    private double finalSalePrice;   // 0 cho đến khi phiên kết thúc
    private Status status;

    // ── Constructor ───────────────────────────────────────────────────────────

    public SellerRole(User user, String auctionId, String itemId) {
        this.user          = user;
        this.auctionId     = auctionId;
        this.itemId        = itemId;
        this.winnerId      = null;
        this.finalSalePrice = 0.0;
        this.status        = Status.WAITING;
    }

    // ── AuctionRole ───────────────────────────────────────────────────────────

    @Override public String getRoleName()  { return "SELLER"; }
    @Override public String getAuctionId() { return auctionId; }
    @Override public String getUserId()    { return user.getEntityId(); }

    // ── Observer ──────────────────────────────────────────────────────────────

    @Override
    public void onAuctionEvent(AuctionEvent event) {
        switch (event.getType()) {

            case AUCTION_STARTED -> {
                status = Status.ONGOING;
                System.out.println("[SELLER] Phiên " + auctionId
                        + " của " + user.getUsername() + " bắt đầu!");
            }

            case AUCTION_CLOSED -> {
                String winner = event.getData("winnerId", String.class);
                Double price  = event.getData("finalPrice", Double.class);

                if (winner != null && price != null && price > 0) {
                    this.winnerId       = winner;
                    this.finalSalePrice = price;
                    this.status         = Status.SOLD;
                    System.out.println("[SELLER] Bán thành công! Giá: "
                            + String.format("%,.0f VNĐ", price));
                } else {
                    this.status = Status.UNSOLD;
                    System.out.println("[SELLER] Phiên kết thúc — không có người mua.");
                }
            }

            case AUCTION_CANCELLED -> {
                this.status = Status.CANCELLED;
                System.out.println("[SELLER] Phiên " + auctionId + " bị hủy.");
            }

            case BID_PLACED -> {
                Double amount = event.getData("bidAmount", Double.class);
                String name   = event.getData("bidderName", String.class);
                System.out.println("[SELLER NOTIFY] " + name
                        + " đặt " + String.format("%,.0f VNĐ", amount));
            }

            default -> {}
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public User getUser()             { return user; }
    public String getItemId()         { return itemId; }
    public String getWinnerId()       { return winnerId; }
    public double getFinalSalePrice() { return finalSalePrice; }
    public Status getStatus()         { return status; }

    public boolean isSold()      { return status == Status.SOLD; }
    public boolean isUnsold()    { return status == Status.UNSOLD; }
    public boolean isCancelled() { return status == Status.CANCELLED; }
}