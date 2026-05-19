package org.tamtamcatworks.auction.service.mapper;

import org.mapstruct.Mapper;
import org.tamtamcatworks.auction.model.user.BuyerProfile;
import org.tamtamcatworks.auction.model.user.SellerProfile;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default User toEntity(RegisterRequest request, String encodedPassword) {
        User user = new User(
            request.username(),
            request.email(),
            encodedPassword,
            request.fullName(),
            0.0
        );
        user.setBuyerProfile(new BuyerProfile());
        user.setSellerProfile(new SellerProfile());
        return user;
    }

    UserResponse toResponse(User user);
}
