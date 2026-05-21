package org.tamtamcatworks.auction.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.tamtamcatworks.auction.shared.request.AuctionRequest;
import org.tamtamcatworks.auction.shared.request.BidRequest;
import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.request.LoginRequest;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;
import org.tamtamcatworks.auction.shared.response.BidResponse;
import org.tamtamcatworks.auction.shared.response.ItemResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.util.List;

public class ApiClient {

    private final RestClient client;

    public ApiClient() {
        String baseUrl = System.getProperty("api.baseUrl", "http://localhost:8080");

        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

        this.client = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(new JdkClientHttpRequestFactory(httpClient))
            .messageConverters(converters ->
                converters.add(new MappingJackson2HttpMessageConverter(mapper))
            )
            .build();
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    public UserResponse login(LoginRequest request) {
        return client.post()
            .uri("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(UserResponse.class);
    }

    public UserResponse register(RegisterRequest request) {
        return client.post()
            .uri("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(UserResponse.class);
    }

    // ── Auctions ──────────────────────────────────────────────────────────────

    public List<AuctionResponse> getAllAuctions() {
        List<AuctionResponse> all = new java.util.ArrayList<>();
        try {
            List<AuctionResponse> active = getAuctionsByStatus("ACTIVE");
            if (active != null) {
                all.addAll(active);
            }
        } catch (Exception ignored) {
            // ignore
        }
        try {
            List<AuctionResponse> pending = getAuctionsByStatus("PENDING");
            if (pending != null) {
                all.addAll(pending);
            }
        } catch (Exception ignored) {
            // ignore
        }
        try {
            List<AuctionResponse> closed = getAuctionsByStatus("CLOSED");
            if (closed != null) {
                all.addAll(closed);
            }
        } catch (Exception ignored) {
            // ignore
        }
        try {
            List<AuctionResponse> cancelled = getAuctionsByStatus("CANCELLED");
            if (cancelled != null) {
                all.addAll(cancelled);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return all;
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

    public AuctionResponse createAuctionWithItem(CreateAuctionRequest request) {
        return client.post()
            .uri("/auctions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(AuctionResponse.class);
    }

    public List<AuctionResponse> getAuctionsByStatus(String status) {
        return client.get()
            .uri("/auctions?status={status}", status)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    public AuctionResponse openAuction(String id) {
        return client.patch()
            .uri("/auctions/{id}/open", id)
            .retrieve()
            .body(AuctionResponse.class);
    }

    public AuctionResponse closeAuction(String id) {
        return client.patch()
            .uri("/auctions/{id}/close", id)
            .retrieve()
            .body(AuctionResponse.class);
    }

    // ── Bids ──────────────────────────────────────────────────────────────────

    public BidResponse placeBid(String auctionId, BidRequest request) {
        return client.post()
            .uri("/auctions/{id}/bids", auctionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(BidResponse.class);
    }

    public List<BidResponse> getBids(String auctionId) {
        return client.get()
            .uri("/auctions/{id}/bids", auctionId)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
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
