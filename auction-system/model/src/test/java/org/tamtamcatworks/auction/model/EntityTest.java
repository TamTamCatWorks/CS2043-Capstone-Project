package org.tamtamcatworks.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTest {
    // Minimal subclass for testing — Engineering should not modify this
    static class TestEntity extends BaseEntity {
        @Override
        public String getDisplayInfo() {
            return "TestEntity[" + getId() + "]";
        }
    }

    TestEntity entity;

    @BeforeEach
    void setUp() {
        entity = new TestEntity();
    }

    @Test
    void shouldReturnNonEmptyDisplayInfo() {
        assertFalse(entity.getDisplayInfo().isBlank());
    }
}
