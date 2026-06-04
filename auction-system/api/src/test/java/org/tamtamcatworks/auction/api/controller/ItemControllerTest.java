package org.tamtamcatworks.auction.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.tamtamcatworks.auction.service.item.ItemService;
import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.response.ItemResponse;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureDataJpa
class ItemControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private ItemService itemService;

  @Test
  void testCreateItem() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("userId", "seller123");
    ItemRequest request =
        new ItemRequest("ART", "Starry Paint", "Rep paint", 200.0, "GOOD", null, "img", Map.of());
    ItemResponse response =
        new ItemResponse(
            "item123", "Starry Paint", "Rep paint", 200.0, "GOOD", "seller123", "Starry", "img");
    when(itemService.createResponse(any(ItemRequest.class))).thenReturn(response);
    mockMvc
        .perform(
            post("/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("item123"))
        .andExpect(jsonPath("$.name").value("Starry Paint"));
  }

  @Test
  void testGetItem() throws Exception {
    ItemResponse response =
        new ItemResponse(
            "item123", "Starry Paint", "Rep paint", 200.0, "GOOD", "seller123", "Starry", "img");
    when(itemService.findResponseById("item123")).thenReturn(response);
    mockMvc
        .perform(get("/items/item123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("item123"));
  }
}
