package org.tamtamcatworks.auction.client.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * WebSocket client for subscribing to real-time auction updates via STOMP.
 */
public class AuctionWebSocketClient {

    private final WebSocketStompClient stompClient;
    private StompSession session;

    @SuppressWarnings("null")
    public AuctionWebSocketClient() {
        SockJsClient sockJsClient = new SockJsClient(List.of(new RestTemplateXhrTransport()));
        stompClient = new WebSocketStompClient(sockJsClient);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    /**
     * Connects to the WebSocket server asynchronously.
     *
     * @return a CompletableFuture resolving to the StompSession.
     */
    public CompletableFuture<StompSession> connect() {
        String baseUrl = System.getProperty("api.baseUrl", "http://localhost:8080");
        String wsUrl = baseUrl + "/ws";

        CompletableFuture<StompSession> future = new CompletableFuture<>();

        stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(@NonNull StompSession stompSession, @NonNull StompHeaders connectedHeaders) {
                session = stompSession;
                future.complete(stompSession);
            }

            @Override
            public void handleException(@NonNull StompSession stompSession, @Nullable StompCommand command,
                    @NonNull StompHeaders headers, @Nullable byte[] payload, @NonNull Throwable exception) {
                future.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(@NonNull StompSession stompSession, @NonNull Throwable exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    /**
     * Subscribes to live price updates for a specific auction.
     *
     * @param auctionId the ID of the auction to watch
     * @param onPriceUpdate callback when a price update is received
     * @return the subscription handle, or null if not connected
     */
    public StompSession.Subscription subscribeToPrice(String auctionId, Consumer<AuctionPriceUpdate> onPriceUpdate) {
        if (session == null || !session.isConnected()) {
            return null;
        }

        return session.subscribe("/topic/auctions/" + auctionId, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return AuctionPriceUpdate.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                if (payload instanceof AuctionPriceUpdate update) {
                    onPriceUpdate.accept(update);
                }
            }
        });
    }

    /**
     * Subscribes to live status updates for a specific auction.
     *
     * @param auctionId the ID of the auction to watch
     * @param onStatusUpdate callback when a status update is received
     * @return the subscription handle, or null if not connected
     */
    public StompSession.Subscription subscribeToStatus(String auctionId, Consumer<AuctionStatusUpdate> onStatusUpdate) {
        if (session == null || !session.isConnected()) {
            return null;
        }

        return session.subscribe("/topic/auctions/" + auctionId + "/status", new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return AuctionStatusUpdate.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                if (payload instanceof AuctionStatusUpdate update) {
                    onStatusUpdate.accept(update);
                }
            }
        });
    }

    /**
     * Subscribes to the current user's personal notification queue.
     *
     * @param onNotification callback when a notification arrives
     * @return the subscription handle, or null if not connected
     */
    public StompSession.Subscription subscribeToNotifications(Consumer<NotificationResponse> onNotification) {
        if (session == null || !session.isConnected()) {
            return null;
        }

        return session.subscribe("/user/queue/notifications", new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return NotificationResponse.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                if (payload instanceof NotificationResponse notification) {
                    onNotification.accept(notification);
                }
            }
        });
    }

    /**
     * Subscribes to real-time updates for the currently logged-in user.
     *
     * @param onUserState callback when a fresh user snapshot arrives
     * @return the subscription handle, or null if not connected
     */
    public StompSession.Subscription subscribeToUserState(String userId,
            Consumer<UserResponse> onUserState) {
        if (session == null || !session.isConnected()) {
            return null;
        }

        return session.subscribe("/topic/user-state/" + userId, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return UserResponse.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                if (payload instanceof UserResponse userState) {
                    onUserState.accept(userState);
                }
            }
        });
    }

    /**
     * Disconnects the WebSocket session.
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    // --- Inner Payload DTOs matching the server-side broadcast ---

    public record AuctionPriceUpdate(String auctionId, double newPrice, String timestamp) {}
    public record AuctionStatusUpdate(String auctionId, String newStatus, String reason) {}
}
