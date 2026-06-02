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

    default UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        boolean isAdmin = user.getAdminProfile() != null;
        java.util.List<String> permissions = isAdmin ? user.getAdminProfile().getPermissions() : java.util.List.of();
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getBalance(),
            user.getHoldBalance(),
            user.isActive(),
            isAdmin,
            permissions
        );
    }
}
