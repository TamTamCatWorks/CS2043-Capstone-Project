package org.tamtamcatworks.auction.model.notification;

public enum NotificationType {
    BID_PLACED,      // sent to seller when someone bids on their auction
    OUTBID,          // sent to previous leading bidder
    AUCTION_OPENED,  // sent to seller
    AUCTION_CLOSED,  // sent to seller + leading bidder
    AUCTION_CANCELLED
}
