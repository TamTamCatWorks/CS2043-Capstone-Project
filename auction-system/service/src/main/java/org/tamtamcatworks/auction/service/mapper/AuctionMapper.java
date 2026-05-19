package org.tamtamcatworks.auction.service.mapper;

import org.mapstruct.Mapper;
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

    default AuctionResponse toResponse(Auction auction) {
        return new AuctionResponse(
            auction.getTitle(),
            auction.getSeller().getId(),
            auction.getSeller().getFullName(),
            auction.getItem().getId(),
            auction.getItem().getName(),
            auction.getStartingPrice(),
            auction.getStartTime(),
            auction.getEndTime()
        );
    }
}
