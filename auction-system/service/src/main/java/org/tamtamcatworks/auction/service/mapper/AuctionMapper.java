package org.tamtamcatworks.auction.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

@Mapper(componentModel = "spring")
public interface AuctionMapper {

    default Auction toEntity(CreateAuctionRequest request, User seller, Item item) {
        return new Auction(
            request.title(),
            seller,
            item,
            request.item().startingPrice(),
            request.startTime(),
            request.endTime()
        );
    }

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "sellerName", source = "seller.fullName")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "leadingBidderId", source = "leadingBidder.id")
    @Mapping(target = "leadingBidderName", source = "leadingBidder.fullName")
    AuctionResponse toResponse(Auction auction);
}
