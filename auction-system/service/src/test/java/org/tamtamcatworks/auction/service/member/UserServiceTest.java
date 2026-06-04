package org.tamtamcatworks.auction.service.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.auction.AuctionService;
import org.tamtamcatworks.auction.service.mapper.UserMapper;
import org.tamtamcatworks.auction.shared.response.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  @Mock
  private AuctionService auctionService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, passwordEncoder, userMapper, eventPublisher, null, null);
  }

  @Test
  void testRegisterSuccess() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("tester")).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("hashedPass");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User user = userService.register("tester", "test@example.com", "password", "Test User");
    assertNotNull(user);
    assertEquals("tester", user.getUsername());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("hashedPass", user.getPasswordHash());
  }

  @Test
  void testRegisterDuplicateEmailThrowsException() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> 
        userService.register("tester", "test@example.com", "password", "Test User")
    );
  }

  @Test
  void testRegisterDuplicateUsernameThrowsException() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("tester")).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> 
        userService.register("tester", "test@example.com", "password", "Test User")
    );
  }

  @Test
  void testLoginSuccess() {
    User user = new User("tester", "test@example.com", "hashedPass", "Test User", 0.0);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "hashedPass")).thenReturn(true);

    User loggedIn = userService.login("test@example.com", "password");
    assertEquals(user, loggedIn);
  }

  @Test
  void testLoginWrongPasswordThrowsException() {
    User user = new User("tester", "test@example.com", "hashedPass", "Test User", 0.0);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpassword", "hashedPass")).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> 
        userService.login("test@example.com", "wrongpassword")
    );
  }

  @Test
  void testTopUp() {
    User user = new User("tester", "test@example.com", "hashedPass", "Test User", 10.0);
    when(userRepository.findById("id123")).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    
    UserResponse mockResponse = new UserResponse("id123", "tester", "test@example.com", "Test User", 110.0, 0.0, true, false, List.of());
    when(userMapper.toResponse(any(User.class))).thenReturn(mockResponse);

    UserResponse response = userService.topUp("id123", 100.0);
    assertEquals(110.0, response.balance());
    verify(eventPublisher, times(1)).publishEvent(any(org.tamtamcatworks.auction.service.event.UserStateEvent.class));
  }

  @Test
  void testPromoteToAdmin() {
    User user = new User("tester", "test@example.com", "hashedPass", "Test User", 10.0);
    when(userRepository.findById("id123")).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    userService.promoteToAdmin("id123", List.of("MANAGE_USERS"));
    assertNotNull(user.getAdminProfile());
    assertTrue(user.getAdminProfile().getPermissions().contains("MANAGE_USERS"));
    
    List<String> logs = userService.getAdminActionLogs("id123");
    assertEquals(1, logs.size());
    assertTrue(logs.get(0).contains("Promoted to admin"));
  }
}
