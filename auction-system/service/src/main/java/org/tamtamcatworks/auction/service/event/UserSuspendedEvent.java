package org.tamtamcatworks.auction.service.event;

/**
 * Published by {@link org.tamtamcatworks.auction.service.member.UserService#suspendUser(String)}
 * after a user's account has been deactivated and the change committed to the database.
 *
 * <p>Consumed by:
 * <ul>
 *   <li>{@code SuspendedUserWsHandler} — to force-close any live WebSocket sessions for the user.</li>
 * </ul>
 */
public record UserSuspendedEvent(String userId) {}
