package org.tamtamcatworks.auction.api.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry mapping {@code userId → Set<WebSocketSession>}.
 *
 * <p>Also implements {@link WebSocketHandlerDecoratorFactory} so it can be
 * registered directly in {@link WebSocketConfig#configureWebSocketTransport}.
 * The decorator intercepts {@code afterConnectionEstablished} / {@code afterConnectionClosed}
 * to automatically keep the registry in sync with live sessions.
 *
 * <p>Only authenticated users (those whose {@link WebSocketSession#getPrincipal()} is non-null)
 * are tracked; anonymous connections are ignored.
 */
@Component
public class WebSocketSessionRegistry implements WebSocketHandlerDecoratorFactory {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions =
            new ConcurrentHashMap<>();

    private final UserRepository userRepository;

    public WebSocketSessionRegistry(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── WebSocketHandlerDecoratorFactory ─────────────────────────────────────

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                registerSession(session);
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session,
                                              CloseStatus closeStatus) throws Exception {
                unregisterSession(session);
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }

    // ── Registry operations ───────────────────────────────────────────────────

    /**
     * Registers a session under the user's ID, resolved from the session principal (email).
     * No-op if the session is unauthenticated.
     */
    private void registerSession(WebSocketSession session) {
        if (session.getPrincipal() == null) return;
        String email = session.getPrincipal().getName();
        userRepository.findByEmail(email).ifPresent(user ->
                sessions.computeIfAbsent(user.getId(),
                        id -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                        .add(session)
        );
    }

    /**
     * Removes a session from the registry, cleaning up the user entry if it becomes empty.
     * No-op if the session is unauthenticated.
     */
    private void unregisterSession(WebSocketSession session) {
        if (session.getPrincipal() == null) return;
        String email = session.getPrincipal().getName();
        userRepository.findByEmail(email).ifPresent(user -> {
            Set<WebSocketSession> userSessions = sessions.get(user.getId());
            if (userSessions != null) {
                userSessions.remove(session);
                if (userSessions.isEmpty()) {
                    sessions.remove(user.getId(), userSessions);
                }
            }
        });
    }

    /**
     * Returns a snapshot of all open WebSocket sessions for the given user.
     *
     * @param userId the entity ID of the user
     * @return unmodifiable set; empty if the user has no active sessions
     */
    public Set<WebSocketSession> getSessions(String userId) {
        Set<WebSocketSession> s = sessions.get(userId);
        return s != null ? Collections.unmodifiableSet(s) : Collections.emptySet();
    }
}
