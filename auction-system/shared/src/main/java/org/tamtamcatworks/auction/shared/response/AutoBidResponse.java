package org.tamtamcatworks.auction.shared.response;

/**
 * Thông tin auto-bid của một user cho một phiên đấu giá.
 *
 * @param id ID của auto-bid
 * @param auctionId ID phiên đấu giá
 * @param bidderId ID người đặt auto-bid
 * @param maxBid mức giá tối đa
 * @param increment bước tăng giá
 * @param active còn hiệu lực hay không
 */
public record AutoBidResponse(
    String id,
    String auctionId,
    String bidderId,
    double maxBid,
    double increment, // read-only, là mininum increment của chính auction đó
    boolean active) {}
