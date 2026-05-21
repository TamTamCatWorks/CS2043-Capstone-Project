package org.tamtamcatworks.auction.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.tamtamcatworks.auction.shared.request.AuctionRequest;
import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.request.LoginRequest;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.ItemResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.List;

public class ApiClient {

    private final RestClient client;

    public ApiClient() {
        String baseUrl = System.getProperty("api.baseUrl", "http://localhost:8080");

        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

        this.client = RestClient.builder()
            .baseUrl(baseUrl)
            .messageConverters(converters ->
                converters.add(new MappingJackson2HttpMessageConverter(mapper))
            )
            .build();
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    public UserResponse login(LoginRequest request) {
        return client.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(UserResponse.class);
    }

    public UserResponse register(RegisterRequest request) {
        return client.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(UserResponse.class);
    }

    // ── Auctions ──────────────────────────────────────────────────────────────

    public List<AuctionResponse> getAllAuctions() {
        return client.get()
            .uri("/auctions")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    public AuctionResponse getAuction(String id) {
        return client.get()
            .uri("/auctions/{id}", id)
            .retrieve()
            .body(AuctionResponse.class);
    }

    public AuctionResponse createAuction(AuctionRequest request) {
        return client.post()
            .uri("/auctions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(AuctionResponse.class);
    }

    // ── Items ─────────────────────────────────────────────────────────────────

    public ItemResponse createItem(ItemRequest request) {
        return client.post()
            .uri("/items")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(ItemResponse.class);
    }

    public List<ItemResponse> getAllItems() {
        return client.get()
            .uri("/items")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }
}
