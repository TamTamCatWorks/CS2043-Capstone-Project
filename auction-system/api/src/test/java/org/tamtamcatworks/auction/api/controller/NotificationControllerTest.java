package org.tamtamcatworks.auction.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.tamtamcatworks.auction.service.image.ImageStorageService;
import org.tamtamcatworks.auction.service.notification.NotificationService;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureDataJpa
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private NotificationService notificationService;

  @MockBean private ImageStorageService imageStorageService;

  @Test
  void testGetMyNotifications() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("userId", "user123");
    NotificationResponse resp =
        new NotificationResponse(
            "notif123", "OUTBID", "You have been outbid.", false, "2026-06-02T12:00:00");
    when(notificationService.getForUser("user123")).thenReturn(List.of(resp));
    mockMvc
        .perform(get("/notifications").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("notif123"))
        .andExpect(jsonPath("$[0].message").value("You have been outbid."));
  }

  @Test
  void testMarkAllRead() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("userId", "user123");
    mockMvc
        .perform(patch("/notifications/read-all").session(session))
        .andExpect(status().isNoContent());
    verify(notificationService, times(1)).markAllRead("user123");
  }

  @Test
  void testMarkOneRead() throws Exception {
    mockMvc.perform(patch("/notifications/notif123/read")).andExpect(status().isNoContent());
    verify(notificationService, times(1)).markOneRead("notif123");
  }
}
