package org.tamtamcatworks.auction.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.user.User;

import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;

@Mapper(
    componentModel = "spring",
    uses = ItemMapper.class
)
public interface AuctionMapper {

    default Auction toEntity(
        CreateAuctionRequest request,
        User seller,
        Item item
    ) {

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
    @Mapping(target = "imageUrl", source = "item.imageUrl")
    @Mapping(target = "itemDescription", source = "item.description")
    @Mapping(target = "itemType", expression = "java(auction.getItem() != null ? auction.getItem().getClass().getSimpleName() : null)")
    @Mapping(target = "specificInfo", expression = "java(auction.getItem() != null ? auction.getItem().getSpecificInfo() : null)")
    @Mapping(target = "leadingBidderId", source = "leadingBidder.id")
    @Mapping(target = "leadingBidderName", source = "leadingBidder.fullName")
    AuctionResponse toResponse(Auction auction);

    default PageResponse<AuctionResponse> toPageResponse(Page<Auction> page) {
        List<AuctionResponse> content = page.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
