package org.tamtamcatworks.auction.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.tamtamcatworks.auction.service.auction.AuctionService;
import org.tamtamcatworks.auction.service.auction.AutoBidService;
import org.tamtamcatworks.auction.service.auction.BidService;
import org.tamtamcatworks.auction.shared.request.AutoBidRequest;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.AutoBidResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import java.time.LocalDateTime;

@WebMvcTest(controllers = AuctionController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureDataJpa
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private BidService bidService;

    @MockBean
    private AutoBidService autoBidService;

    @Test
    void testGetAuctionEndpoint() throws Exception {
        AuctionResponse resp = new AuctionResponse("auc123", "Comic Sale", "seller123", "John Doe", "item123", "Vintage Comic Book", null, null, 100.0, 100.0, 1000.0, "ACTIVE", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "http://img.com", "Description", "Collectibles", "Info");

        when(auctionService.findResponseById("auc123")).thenReturn(resp);
        mockMvc.perform(get("/auctions/auc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("auc123"))
                .andExpect(jsonPath("$.title").value("Comic Sale"));
    }
    @Test
    void testOpenAuctionEndpoint() throws Exception {
        AuctionResponse resp = new AuctionResponse("auc123", "Comic Sale", "seller123", "John Doe", "item123", "Vintage Comic Book", null, null, 100.0, 100.0, 1000.0, "ACTIVE", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "http://img.com", "Description", "Collectibles", "Info");

        when(auctionService.openById("auc123")).thenReturn(resp);
        mockMvc.perform(patch("/auctions/auc123/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    @Test
    void testPlaceBidEndpoint() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "bidder123");
        BidRequest req = new BidRequest(120.0, "MANUAL");
        BidResponse resp = new BidResponse("bid123", "auc123", "bidder123", "John Doe", 120.0, "MANUAL", LocalDateTime.now());

        when(bidService.placeBid(eq("auc123"), eq("bidder123"), any(BidRequest.class))).thenReturn(resp);
        mockMvc.perform(post("/auctions/auc123/bids")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("bid123"))
                .andExpect(jsonPath("$.amount").value(120.0));
    }

    @Test
    void testRegisterAutoBidEndpoint() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "bidder123");
        AutoBidRequest req = new AutoBidRequest(1500.0);
        AutoBidResponse resp = new AutoBidResponse("auto123", "auc123", "bidder123", 1500.0, 1000.0, true);

        when(autoBidService.register(eq("auc123"), eq("bidder123"), any(AutoBidRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/auctions/auc123/auto-bid")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("auto123"))
                .andExpect(jsonPath("$.maxBid").value(1500.0))
                .andExpect(jsonPath("$.increment").value(1000.0));
    }
}

