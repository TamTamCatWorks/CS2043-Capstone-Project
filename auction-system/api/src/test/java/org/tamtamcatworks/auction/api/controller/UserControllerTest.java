package org.tamtamcatworks.auction.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.tamtamcatworks.auction.service.member.UserService;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureDataJpa
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  @MockBean private AuthenticationManager authenticationManager;

  @MockBean private SecurityContextRepository securityContextRepository;

  @MockBean private UserDetailsService userDetailsService; // needed for security context if loaded

  @Test
  void testRegisterUserEndpoint() throws Exception {
    RegisterRequest req =
        new RegisterRequest("alice", "alice@example.com", "pass123", "Alice Smith");
    UserResponse resp =
        new UserResponse(
            "user123", "alice", "alice@example.com", "Alice Smith", 0.0, 0.0, true, false, null);
    when(userService.registerByRequest(any(RegisterRequest.class))).thenReturn(resp);
    mockMvc
        .perform(
            post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("user123"))
        .andExpect(jsonPath("$.username").value("alice"));
  }

  @Test
  void testGetUserEndpoint() throws Exception {
    UserResponse resp =
        new UserResponse(
            "user123", "alice", "alice@example.com", "Alice Smith", 100.0, 0.0, true, false, null);
    when(userService.findResponseById("user123")).thenReturn(resp);
    mockMvc
        .perform(get("/users/user123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(100.0));
  }
}
