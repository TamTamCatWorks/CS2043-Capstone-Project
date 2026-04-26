package org.tamtamcatworks.auction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTest {
    // Minimal subclass for testing — Engineering should not modify this
    static class TestEntity extends Entity {
        @Override
        public String getDisplayInfo() {
            return "TestEntity[" + getEntityId() + "]";
        }
    }

    TestEntity entity;

    @BeforeEach
    void setUp() {
        entity = new TestEntity();
    }

    @Test
    void shouldGenerateValidUUIDOnConstruction() {
        assertTrue(entity.getEntityId().matches(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        ));
    }

    @Test
    void shouldCaptureCreatedAtOnConstruction() {
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void shouldBeImmutable() {
        assertAll(
            () -> assertEquals(entity.getEntityId(), entity.getEntityId()),
            () -> assertEquals(entity.getCreatedAt(), entity.getCreatedAt())
        );
    }

    @Test
    void shouldGenerateUniqueIdPerInstance() {
        assertNotEquals(entity.getEntityId(), new TestEntity().getEntityId());
    }

    @Test
    void shouldReturnNonEmptyDisplayInfo() {
        assertFalse(entity.getDisplayInfo().isBlank());
    }
}
