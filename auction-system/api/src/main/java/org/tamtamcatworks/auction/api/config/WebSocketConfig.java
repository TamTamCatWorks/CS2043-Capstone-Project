package org.tamtamcatworks.auction.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketSessionRegistry sessionRegistry;

    public WebSocketConfig(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic  → public broadcast (e.g. auction price updates)
        // /queue  → user-private messages (e.g. personal notifications)
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // SockJS fallback for non-WS environments
    }

    /**
     * Registers {@link WebSocketSessionRegistry} as a decorator factory so it can
     * intercept raw WebSocket connection lifecycle events and maintain the
     * userId → session map used by {@link SuspendedUserWsHandler}.
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(sessionRegistry);
    }
}
