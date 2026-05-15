package org.tamtamcatworks.auction.api.dto;

import org.tamtamcatworks.auction.model.BidTransaction;

public record BidRequest(double amount, BidTransaction.BidType bidType) {}
