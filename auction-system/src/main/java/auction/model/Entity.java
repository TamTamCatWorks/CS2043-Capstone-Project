package auction.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Abstract Classes & Inheritance                             │
 * │                                                                         │
 * │  Entity is the root of the domain object hierarchy. It captures         │
 * │  behaviour and data that is common to EVERY persistent object           │
 * │  (users, items, auctions, transactions).                                │
 * │                                                                         │
 * │  STUDENT QUESTIONS — answer these in your design doc or as comments:    │
 * │                                                                         │
 * │  Q1. Why is Entity declared abstract rather than as a regular class?    │
 * │      What would happen if someone called `new Entity()` directly?       │
 * │                                                                         │
 * │  Q2. The fields entityId and createdAt are described as "immutable      │
 * │      after construction". Which Java keywords or techniques enforce      │
 * │      this? (Hint: think about `final` and access modifiers.)            │
 * │                                                                         │
 * │  Q3. getDisplayInfo() is abstract here but getEntityId() and            │
 * │      getCreatedAt() are concrete. Explain the design decision: when     │
 * │      should a method in an abstract class be abstract vs concrete?      │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public abstract class Entity {

    // TODO: declare entityId (auto-generated UUID, immutable)
    // TODO: declare createdAt (timestamp set at construction, immutable)

    /**
     * TODO: Constructor — generate a UUID for entityId and capture
     * LocalDateTime.now() for createdAt. Both must never change after this.
     */
    protected Entity() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the unique identifier for this entity.
     */
    public String getEntityId() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a human-readable summary of this entity.
     * Each concrete subclass MUST provide its own implementation.
     */
    public abstract String getDisplayInfo();
}
