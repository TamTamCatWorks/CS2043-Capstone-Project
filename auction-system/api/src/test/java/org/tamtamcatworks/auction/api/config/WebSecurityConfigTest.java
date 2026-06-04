package org.tamtamcatworks.auction.api.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.tamtamcatworks.auction.api.controller.AdminController;
import org.tamtamcatworks.auction.api.controller.NotificationController;
import org.tamtamcatworks.auction.api.controller.UserController;
import org.tamtamcatworks.auction.service.auction.AuctionService;
import org.tamtamcatworks.auction.service.member.UserService;
import org.tamtamcatworks.auction.service.notification.NotificationService;

/**
 * Tests for {@link WebSecurityConfig}.
 *
 * <p>Uses {@code @WebMvcTest} with specific controllers so only the chosen controllers and the
 * security filter chain are loaded into the context. Security filters are kept <em>active</em> (no
 * {@code addFilters = false}) so authorization rules can be verified end-to-end with MockMvc.
 *
 * <p>Because Spring Security filters run <em>before</em> controller dispatch, 401 / 403 responses
 * are produced by the filter chain regardless of whether a handler method exists for the path —
 * allowing comprehensive coverage of all configured routes from a single test class.
 */
@WebMvcTest(
    controllers = {UserController.class, AdminController.class, NotificationController.class})
@Import(WebSecurityConfig.class)
@AutoConfigureDataJpa
@ActiveProfiles("test")
class WebSecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private SecurityContextRepository securityContextRepository;

  @Autowired private SecurityFilterChain securityFilterChain;

  // ── Service mocks required by the loaded controllers ──────────────────────

  @MockBean private UserService userService;

  @MockBean private AuctionService auctionService;

  @MockBean private NotificationService notificationService;

  // ── Security infrastructure mocks ─────────────────────────────────────────

  @MockBean private UserDetailsService userDetailsService;

  @MockBean private AuthenticationManager authenticationManager;

  // ── Custom ResultMatchers ─────────────────────────────────────────────────

  /** Asserts the response status is NOT 401 Unauthorized. */
  private static ResultMatcher isNotUnauthorized() {
    return result ->
        assertNotEquals(
            401, result.getResponse().getStatus(), "Expected status to not be 401 Unauthorized");
  }

  /** Asserts the response status is NOT 403 Forbidden. */
  private static ResultMatcher isNotForbidden() {
    return result ->
        assertNotEquals(
            403, result.getResponse().getStatus(), "Expected status to not be 403 Forbidden");
  }

  // ── Bean presence ─────────────────────────────────────────────────────────

  @Test
  void passwordEncoderBeanIsCreated() {
    assertNotNull(passwordEncoder);
  }

  @Test
  void securityContextRepositoryBeanIsHttpSessionBased() {
    assertNotNull(securityContextRepository);
    assertInstanceOf(HttpSessionSecurityContextRepository.class, securityContextRepository);
  }

  @Test
  void securityFilterChainBeanIsCreated() {
    assertNotNull(securityFilterChain);
  }

  // ── PasswordEncoder ───────────────────────────────────────────────────────

  @Test
  void passwordEncoderEncodesAndMatchesCorrectly() {
    String raw = "s3cr3t!";
    String encoded = passwordEncoder.encode(raw);

    assertTrue(passwordEncoder.matches(raw, encoded));
  }

  @Test
  void passwordEncoderDoesNotMatchWrongPassword() {
    String encoded = passwordEncoder.encode("correct");

    assertTrue(!passwordEncoder.matches("wrong", encoded));
  }

  @Test
  void passwordEncoderProducesDifferentHashEachTime() {
    String raw = "password";
    String hash1 = passwordEncoder.encode(raw);
    String hash2 = passwordEncoder.encode(raw);

    // BCrypt includes a random salt so each hash must differ
    assertTrue(!hash1.equals(hash2));
  }

  // ── AuthenticationManager ─────────────────────────────────────────────────

  @Test
  void authenticationManagerAuthenticatesValidCredentials() {
    String email = "user@example.com";
    String rawPassword = "password";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    UserDetails userDetails =
        User.withUsername(email).password(encodedPassword).roles("USER").build();
    when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

    WebSecurityConfig config = new WebSecurityConfig();
    AuthenticationManager authManager =
        assertDoesNotThrow(() -> config.authenticationManager(userDetailsService, passwordEncoder));

    assertDoesNotThrow(
        () ->
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword)));
  }

  // ── Public endpoints (no auth needed) ────────────────────────────────────

  @Test
  void registerEndpointIsPublic() throws Exception {
    mockMvc.perform(post("/users/register")).andExpect(isNotUnauthorized());
  }

  @Test
  void loginEndpointIsPublic() throws Exception {
    mockMvc.perform(post("/users/login")).andExpect(isNotUnauthorized());
  }

  @Test
  void getItemByIdIsPublic() throws Exception {
    // Security passes the request; 404 is returned because no ItemController
    // is loaded — but the important assertion is the absence of 401.
    mockMvc.perform(get("/items/some-id")).andExpect(isNotUnauthorized());
  }

  @Test
  void getAuctionsIsPublic() throws Exception {
    mockMvc.perform(get("/auctions")).andExpect(isNotUnauthorized());
  }

  @Test
  void getAuctionByIdIsPublic() throws Exception {
    mockMvc.perform(get("/auctions/some-id")).andExpect(isNotUnauthorized());
  }

  @Test
  void getAuctionBidsIsPublic() throws Exception {
    mockMvc.perform(get("/auctions/some-id/bids")).andExpect(isNotUnauthorized());
  }

  @Test
  void webSocketPathIsPermitAll() throws Exception {
    mockMvc.perform(get("/ws/info")).andExpect(isNotUnauthorized());
  }

  // ── Protected endpoints (must be authenticated) ───────────────────────────

  @Test
  void notificationsRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/notifications")).andExpect(status().isUnauthorized());
  }

  @Test
  void notificationsAllowsAuthenticatedUser() throws Exception {
    mockMvc
        .perform(get("/notifications").with(user("user@example.com").roles("USER")))
        .andExpect(isNotUnauthorized())
        .andExpect(isNotForbidden());
  }

  @Test
  void postToAuctionBidsRequiresAuthentication() throws Exception {
    mockMvc.perform(post("/auctions/some-id/bids")).andExpect(status().isUnauthorized());
  }

  // ── Admin-only endpoints ──────────────────────────────────────────────────

  @Test
  void adminEndpointRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/admin/users")).andExpect(status().isUnauthorized());
  }

  @Test
  void adminEndpointForbidsRegularUser() throws Exception {
    mockMvc
        .perform(get("/admin/users").with(user("user@example.com").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminEndpointAllowsAdminUser() throws Exception {
    mockMvc
        .perform(get("/admin/users").with(user("admin@example.com").roles("ADMIN")))
        .andExpect(isNotUnauthorized())
        .andExpect(isNotForbidden());
  }

  // ── CSRF exemptions ───────────────────────────────────────────────────────

  @Test
  void csrfIsDisabledForRegisterEndpoint() throws Exception {
    // No CSRF token — must NOT produce 403
    mockMvc.perform(post("/users/register")).andExpect(isNotForbidden());
  }

  @Test
  void csrfIsDisabledForLoginEndpoint() throws Exception {
    mockMvc.perform(post("/users/login")).andExpect(isNotForbidden());
  }

  @Test
  void csrfIsDisabledForCreateAuction() throws Exception {
    mockMvc
        .perform(post("/auctions").with(user("user@example.com").roles("USER")))
        .andExpect(isNotForbidden());
  }

  @Test
  void csrfIsDisabledForCreateItem() throws Exception {
    mockMvc
        .perform(post("/items").with(user("user@example.com").roles("USER")))
        .andExpect(isNotForbidden());
  }

  @Test
  void csrfIsDisabledForWebSocketEndpoint() throws Exception {
    mockMvc.perform(post("/ws/some-path")).andExpect(isNotForbidden());
  }
}
