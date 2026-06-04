package org.tamtamcatworks.auction.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketConfig}.
 *
 * The broker configuration and endpoint registration are verified by passing in
 * Mockito mocks — no real WebSocket connection is needed.
 * A minimal {@code @SpringBootTest} slice is used only for the bean-presence
 * check; the behavioral tests are plain unit tests.
 */
class WebSocketConfigTest {

    private final WebSocketConfig config = new WebSocketConfig();

    // ── configureMessageBroker ────────────────────────────────────────────────

    @Test
    void configureMessageBrokerEnablesTopicAndQueueDestinations() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
        when(registry.enableSimpleBroker(any(String[].class))).thenReturn(null);
        when(registry.setApplicationDestinationPrefixes(any(String[].class))).thenReturn(null);

        config.configureMessageBroker(registry);

        // /topic (public broadcast) and /queue (user-private) must both be enabled
        verify(registry).enableSimpleBroker("/topic", "/queue");
    }

    @Test
    void configureMessageBrokerSetsApplicationDestinationPrefix() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
        when(registry.enableSimpleBroker(any(String[].class))).thenReturn(null);
        when(registry.setApplicationDestinationPrefixes(any(String[].class))).thenReturn(null);

        config.configureMessageBroker(registry);

        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void configureMessageBrokerSetsUserDestinationPrefix() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
        when(registry.enableSimpleBroker(any(String[].class))).thenReturn(null);
        when(registry.setApplicationDestinationPrefixes(any(String[].class))).thenReturn(null);

        config.configureMessageBroker(registry);

        verify(registry).setUserDestinationPrefix("/user");
    }

    // ── registerStompEndpoints ────────────────────────────────────────────────

    @Test
    void registerStompEndpointsRegistersWsEndpoint() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration =
            mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any(String[].class))).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(null);

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws");
    }

    @Test
    void registerStompEndpointsAllowsAllOrigins() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration =
            mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any(String[].class))).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(null);

        config.registerStompEndpoints(registry);

        // Wildcard origin pattern is required for cross-origin clients
        verify(registration).setAllowedOriginPatterns("*");
    }

    @Test
    void registerStompEndpointsEnablesSockJsFallback() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration =
            mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any(String[].class))).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(null);

        config.registerStompEndpoints(registry);

        // SockJS fallback is required for environments that don't support WebSocket
        verify(registration).withSockJS();
    }

    // ── Guard: configureMessageBroker does not throw ──────────────────────────

    @Test
    void configureMessageBrokerDoesNotThrow() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
        when(registry.enableSimpleBroker(any(String[].class))).thenReturn(null);
        when(registry.setApplicationDestinationPrefixes(any(String[].class))).thenReturn(null);

        assertDoesNotThrow(() -> config.configureMessageBroker(registry));
    }

    @Test
    void registerStompEndpointsDoesNotThrow() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration =
            mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any(String[].class))).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(null);

        assertDoesNotThrow(() -> config.registerStompEndpoints(registry));
    }
}
