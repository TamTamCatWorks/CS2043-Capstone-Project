package org.tamtamcatworks.auction.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.tamtamcatworks.auction.service.event.UserSuspendedEvent;

import java.io.IOException;
import java.util.Set;

/**
 * Application-event listener that force-closes all active WebSocket sessions
 * belonging to a user whose account has just been suspended.
 *
 * <p>Listens for {@link UserSuspendedEvent} (published by
 * {@link org.tamtamcatworks.auction.service.member.UserService#suspendUser(String)}
 * after the database transaction commits).
 *
 * <p>For each live session found in {@link WebSocketSessionRegistry}:
 * <ol>
 *   <li>A close frame with status {@code 1008 Policy Violation} and reason
 *       {@code "Account suspended"} is sent to the client.</li>
 *   <li>The underlying WebSocket connection is closed.</li>
 * </ol>
 *
 * <p>Uses constructor injection; no {@code @Autowired} on fields.
 */
@Component
public class SuspendedUserWsHandler {

    private static final Logger log = LoggerFactory.getLogger(SuspendedUserWsHandler.class);

    /** 1008 Policy Violation — the closest standard close code for "you are not allowed here". */
    private static final CloseStatus SUSPENDED_CLOSE_STATUS =
            new CloseStatus(CloseStatus.POLICY_VIOLATION.getCode(), "Account suspended");

    private final WebSocketSessionRegistry sessionRegistry;

    public SuspendedUserWsHandler(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Handles the {@link UserSuspendedEvent}.
     *
     * <p>Iterates over a snapshot of the user's sessions (to avoid
     * {@link java.util.ConcurrentModificationException} as the registry updates
     * on close) and closes each open session with an explanatory close frame.
     *
     * @param event the suspension event carrying the affected user's ID
     */
    @EventListener
    public void onUserSuspended(UserSuspendedEvent event) {
        String userId = event.userId();
        Set<WebSocketSession> snapshot = Set.copyOf(sessionRegistry.getSessions(userId));

        if (snapshot.isEmpty()) {
            return;
        }

        log.info("Closing {} WebSocket session(s) for suspended user [{}]", snapshot.size(), userId);

        for (WebSocketSession session : snapshot) {
            if (!session.isOpen()) continue;
            try {
                session.close(SUSPENDED_CLOSE_STATUS); // sends close frame then closes the connection
            } catch (IOException e) {
                log.warn("Failed to close WebSocket session [{}] for user [{}]: {}",
                        session.getId(), userId, e.getMessage());
            }
        }
    }
}
