package org.tamtamcatworks.auction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base class for all domain entities in the system.
 *
 * <p>Provides a universally unique identifier ({@code entityId}) and a
 * creation timestamp ({@code createdAt}). Both are auto-generated
 * at construction and immutable for the lifetime of the object.
 *
 * <p>All model classes will extend this and implement {@link #getDisplayInfo()}
 *
 * @author R&D (Nguyen Hoang Vu)
 * @version 1.0
 * @since 1.0uch as {@code Bidder} and {@code Item} must extend
 * this class and provide an implementation of {@link #getDisplayInfo()}.  
 */
public abstract class Entity {

    /**
     * Unique identifier for this entity, auto-generated as a UUID at construction.
     * Immutable.
     */
    // TODO: declare entityId (String, final)

    /**
     * Timestamp recording when this entity was created.
     * Immutable.
     */
    // TODO: declare createdAt (LocalDateTime, final)

    /**
     * Initializes a new entity by generating a unique {@code entityId}
     * and capturing the current timestamp as {@code createdAt}.
     *
     * <p>Both fields are set exactly once here and must never be modified.
     *
     * @implSpec Use {@code UUID.randomUUID().toString()} for {@code entityId}
     *           and {@code LocalDateTime.now()} for {@code createdAt}.
     */
    protected Entity() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the UUID for this entity.
     *
     * @return the UUID string identifying this entity, be sure to check for {@code null}
     */
    public String getEntityId() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the timestamp when this entity was created.
     *
     * @return the {@link LocalDateTime} of creation, be sure to check for {@code null}
     */
    public LocalDateTime getCreatedAt() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a human-readable summary.
     *
     * <p>Each concrete subclass that extends this must provide its own implementation.
     *
     * @return a string describing this entity
     */
    public abstract String getDisplayInfo();
}