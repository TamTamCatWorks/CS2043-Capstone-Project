package org.tamtamcatworks.auction.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BaseEntity.
 */
public class BaseEntityTest {

  /**
   * Helper class to instantiate BaseEntity.
   */
  private static class TestEntity extends BaseEntity {
    protected TestEntity() {
      super();
    }
  }

  private void setId(BaseEntity entity, String id) throws Exception {
    Field field = BaseEntity.class.getDeclaredField("id");
    field.setAccessible(true);
    field.set(entity, id);
  }

  @Test
  public void testOnCreate() {
    TestEntity entity = new TestEntity();
    assertNull(entity.getCreationDate());
    entity.onCreate();
    assertNotNull(entity.getCreationDate());
  }

  @Test
  public void testEqualsAndHashCode() throws Exception {
    TestEntity entity1 = new TestEntity();
    TestEntity entity2 = new TestEntity();

    // Both IDs are null
    assertFalse(entity1.equals(entity2));
    assertFalse(entity2.equals(entity1));
    assertEquals(0, entity1.hashCode());

    // Reflexivity
    assertTrue(entity1.equals(entity1));

    // Null safety
    assertFalse(entity1.equals(null));

    // Type safety
    assertFalse(entity1.equals("some string"));

    // Set same ID
    setId(entity1, "uuid-1234");
    setId(entity2, "uuid-1234");
    assertTrue(entity1.equals(entity2));
    assertTrue(entity2.equals(entity1));
    assertEquals(entity1.hashCode(), entity2.hashCode());
    assertEquals("uuid-1234".hashCode(), entity1.hashCode());

    // Set different ID
    setId(entity2, "uuid-5678");
    assertFalse(entity1.equals(entity2));
    assertFalse(entity2.equals(entity1));
    assertNotEquals(entity1.hashCode(), entity2.hashCode());

    // One ID is null, one is not
    TestEntity entity3 = new TestEntity();
    assertFalse(entity1.equals(entity3));
    assertFalse(entity3.equals(entity1));
  }

  @Test
  public void testToString() throws Exception {
    TestEntity entity = new TestEntity();
    assertEquals("TestEntity{id='null'}", entity.toString());

    setId(entity, "abc-def");
    assertEquals("TestEntity{id='abc-def'}", entity.toString());
  }
}
