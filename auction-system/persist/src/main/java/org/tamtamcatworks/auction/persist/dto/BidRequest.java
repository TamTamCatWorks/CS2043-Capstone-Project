package org.tamtamcatworks.auction.persist.dto;

import org.tamtamcatworks.auction.model.BidTransaction;

public record BidRequest(double amount, BidTransaction.BidType bidType) {}
