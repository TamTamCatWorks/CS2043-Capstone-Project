package org.tamtamcatworks.auction.api.request;

import org.tamtamcatworks.auction.model.BidTransaction;

public record BidRequest(double amount, BidTransaction.BidType bidType) {
}
