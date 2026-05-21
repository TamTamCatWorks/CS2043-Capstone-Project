package org.tamtamcatworks.auction.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.BidResponse;

@Mapper(componentModel = "spring")
public interface BidMapper {

    default BidTransaction toEntity(BidRequest request, Auction auction, User bidder) {
        return new BidTransaction(
            auction,
            bidder,
            request.amount(),
            BidTransaction.BidType.valueOf(request.bidType())
        );
    }

    @Mapping(target = "auctionId", source = "auction.id")
    @Mapping(target = "bidderId", source = "bidder.id")
    @Mapping(target = "bidderName", source = "bidder.fullName")
    @Mapping(target = "bidType", expression = "java(tx.getBidType().name())")
    @Mapping(target = "createdAt", source = "creationDate")
    BidResponse toResponse(BidTransaction tx);
}