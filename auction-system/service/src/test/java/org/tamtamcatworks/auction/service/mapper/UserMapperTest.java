package org.tamtamcatworks.auction.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.model.user.AdminProfile;
import org.tamtamcatworks.auction.model.user.BuyerProfile;
import org.tamtamcatworks.auction.model.user.SellerProfile;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

class UserMapperTest {

  private final UserMapper userMapper = loadMapper();

  // ── helpers ──────────────────────────────────────────────────────────────

  private static RegisterRequest registerRequest() {
    return new RegisterRequest("johndoe", "john@example.com", "secret", "John Doe");
  }

  private static User regularUser() {
    User user = new User("johndoe", "john@example.com", "hashed_secret", "John Doe", 500.0);
    user.setBuyerProfile(new BuyerProfile());
    user.setSellerProfile(new SellerProfile());
    return user;
  }

  // ── toEntity ─────────────────────────────────────────────────────────────

  @Test
  void toEntityCreatesUserWithCorrectCredentials() {
    RegisterRequest request = registerRequest();

    User user = userMapper.toEntity(request, "hashed_secret");

    assertEquals(request.username(), user.getUsername());
    assertEquals(request.email(), user.getEmail());
    assertEquals("hashed_secret", user.getPasswordHash());
    assertEquals(request.fullName(), user.getFullName());
  }

  @Test
  void toEntitySetsInitialBalanceToZero() {
    User user = userMapper.toEntity(registerRequest(), "hashed_secret");

    assertEquals(0.0, user.getBalance());
  }

  @Test
  void toEntityInitializesHoldBalanceToZero() {
    User user = userMapper.toEntity(registerRequest(), "hashed_secret");

    assertEquals(0.0, user.getHoldBalance());
  }

  @Test
  void toEntityAttachesBuyerProfile() {
    User user = userMapper.toEntity(registerRequest(), "hashed_secret");

    assertNotNull(user.getBuyerProfile());
    assertInstanceOf(BuyerProfile.class, user.getBuyerProfile());
  }

  @Test
  void toEntityAttachesSellerProfile() {
    User user = userMapper.toEntity(registerRequest(), "hashed_secret");

    assertNotNull(user.getSellerProfile());
    assertInstanceOf(SellerProfile.class, user.getSellerProfile());
  }

  @Test
  void toEntityStoresEncodedPasswordNotPlainText() {
    RegisterRequest request = registerRequest();

    User user = userMapper.toEntity(request, "bcrypt$encoded");

    assertEquals("bcrypt$encoded", user.getPasswordHash());
  }

  // ── toResponse ────────────────────────────────────────────────────────────

  @Test
  void toResponseReturnsNullForNullUser() {
    UserResponse response = userMapper.toResponse(null);

    assertNull(response);
  }

  @Test
  void toResponseMapsBasicFieldsCorrectly() {
    User user = regularUser();

    UserResponse response = userMapper.toResponse(user);

    assertEquals(user.getId(), response.id());
    assertEquals(user.getUsername(), response.username());
    assertEquals(user.getEmail(), response.email());
    assertEquals(user.getFullName(), response.fullName());
    assertEquals(user.getBalance(), response.balance());
    assertEquals(user.getHoldBalance(), response.holdBalance());
  }

  @Test
  void toResponseReflectsActiveStatusTrue() {
    User user = regularUser();

    UserResponse response = userMapper.toResponse(user);

    assertTrue(response.isActive());
  }

  @Test
  void toResponseReflectsActiveStatusFalse() {
    User user = regularUser();
    user.setActive(false);

    UserResponse response = userMapper.toResponse(user);

    assertFalse(response.isActive());
  }

  @Test
  void toResponseMarksRegularUserAsNonAdmin() {
    User user = regularUser();

    UserResponse response = userMapper.toResponse(user);

    assertFalse(response.isAdmin());
  }

  @Test
  void toResponseReturnsEmptyPermissionsForRegularUser() {
    User user = regularUser();

    UserResponse response = userMapper.toResponse(user);

    assertTrue(response.permissions().isEmpty());
  }

  @Test
  void toResponseMarksAdminUserAsAdmin() {
    User user = regularUser();
    user.setAdminProfile(AdminProfile.superAdmin());

    UserResponse response = userMapper.toResponse(user);

    assertTrue(response.isAdmin());
  }

  @Test
  void toResponseIncludesAdminPermissions() {
    User user = regularUser();
    user.setAdminProfile(AdminProfile.superAdmin());

    UserResponse response = userMapper.toResponse(user);

    List<String> expected =
        List.of("MANAGE_USERS", "MANAGE_ITEMS", "MANAGE_AUCTIONS", "VIEW_LOGS", "MANAGE_ADMINS");
    assertEquals(expected, response.permissions());
  }

  @Test
  void toResponseReflectsUpdatedBalance() {
    User user = regularUser();
    user.addBalance(250.0);

    UserResponse response = userMapper.toResponse(user);

    assertEquals(750.0, response.balance());
  }

  @Test
  void toResponseReflectsHoldBalance() {
    User user = regularUser();
    user.holdFunds(100.0);

    UserResponse response = userMapper.toResponse(user);

    assertEquals(100.0, response.holdBalance());
    assertEquals(400.0, response.balance());
  }

  // ── loader ───────────────────────────────────────────────────────────────

  private static UserMapper loadMapper() {
    try {
      Class<?> mapperImpl =
          Class.forName("org.tamtamcatworks.auction.service.mapper.UserMapperImpl");
      return assertInstanceOf(UserMapper.class, mapperImpl.getDeclaredConstructor().newInstance());
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Unable to load UserMapperImpl", exception);
    }
  }
}
