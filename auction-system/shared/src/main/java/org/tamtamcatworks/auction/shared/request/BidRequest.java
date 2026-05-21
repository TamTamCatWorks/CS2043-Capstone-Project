package org.tamtamcatworks.auction.shared.request;

public record BidRequest(
    double amount,
    String bidType
) {
}
