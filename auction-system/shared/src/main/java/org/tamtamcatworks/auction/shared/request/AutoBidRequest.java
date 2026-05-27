package org.tamtamcatworks.auction.shared.request;

/**
 * Request để đăng ký hoặc cập nhật auto-bid cho một phiên đấu giá.
 *
 * @param maxBid    mức giá tối đa sẵn sàng trả
 * @param increment bước tăng giá mỗi lần tự động đấu
 */
public record AutoBidRequest(double maxBid, double increment) {}
