package org.tamtamcatworks.auction.model.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.tamtamcatworks.auction.model.user.User;

/** Unit tests for Item entity and its subclasses (Art, Electronics, Vehicle, Other). */
public class ItemTest {

  private User createTestSeller() {
    return new User("seller1", "seller@example.com", "hash", "Seller One", 0.0);
  }

  @Test
  public void testItemCondition() {
    assertEquals("Mới", ItemCondition.NEW.getDisplayName());
    assertEquals("Mới", ItemCondition.NEW.toString());
  }

  @Test
  public void testItemCommonValidations() {
    User seller = createTestSeller();

    // Verify successful creation of a concrete subclass (Other)
    Other item =
        new Other(
            "Item Name", "Item Description", 100.0, ItemCondition.NEW, "http://img.url", seller);
    assertEquals("Item Name", item.getName());
    assertEquals("Item Description", item.getDescription());
    assertEquals(100.0, item.getStartingPrice());
    assertEquals(ItemCondition.NEW, item.getCondition());
    assertEquals("http://img.url", item.getImageUrl());
    assertEquals(seller, item.getSeller());
    assertNotNull(item.getListedAt());

    // Validate null/empty name
    assertThrows(
        IllegalArgumentException.class,
        () -> new Other(null, "Desc", 100.0, ItemCondition.NEW, "img", seller));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Other("   ", "Desc", 100.0, ItemCondition.NEW, "img", seller));

    // Validate startingPrice <= 0
    assertThrows(
        IllegalArgumentException.class,
        () -> new Other("Name", "Desc", 0.0, ItemCondition.NEW, "img", seller));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Other("Name", "Desc", -5.0, ItemCondition.NEW, "img", seller));

    // Validate condition cannot be null
    assertThrows(
        IllegalArgumentException.class,
        () -> new Other("Name", "Desc", 100.0, null, "img", seller));

    // Validate image URL cannot be null/empty
    assertThrows(
        IllegalArgumentException.class,
        () -> new Other("Name", "Desc", 100.0, ItemCondition.NEW, null, seller));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Other("Name", "Desc", 100.0, ItemCondition.NEW, "   ", seller));
  }

  @Test
  public void testItemSetters() {
    User seller = createTestSeller();
    Other item = new Other("Original", "Original Desc", 10.0, ItemCondition.GOOD, "img", seller);

    item.setName(" New Name ");
    assertEquals("New Name", item.getName()); // trimmed
    assertThrows(IllegalArgumentException.class, () -> item.setName(null));

    item.setDescription(null);
    assertEquals("", item.getDescription());

    item.setDescription(" New Desc ");
    assertEquals("New Desc", item.getDescription());

    item.setStartingPrice(50.0);
    assertEquals(50.0, item.getStartingPrice());
    assertThrows(IllegalArgumentException.class, () -> item.setStartingPrice(0));

    item.setCondition(ItemCondition.POOR);
    assertEquals(ItemCondition.POOR, item.getCondition());
    assertThrows(IllegalArgumentException.class, () -> item.setCondition(null));

    item.setImageUrl(" new-img ");
    assertEquals("new-img", item.getImageUrl());
    assertThrows(IllegalArgumentException.class, () -> item.setImageUrl(""));

    // PrePersist lifecycle test
    Other itemNoListedAt = new Other() {
          // anonymous concrete class to invoke prePersist
        };
    itemNoListedAt.prePersist();
    assertNotNull(itemNoListedAt.getListedAt());
  }

  @Test
  public void testArtSubclass() {
    User seller = createTestSeller();
    Art art =
        new Art(
            "Mona Lisa",
            "Painting",
            1000000.0,
            ItemCondition.LIKE_NEW,
            "img",
            seller,
            "Da Vinci",
            1503,
            "Oil",
            "77x53cm",
            true);

    assertEquals("Da Vinci", art.getArtist());
    assertEquals(1503, art.getYearCreated());
    assertEquals("Oil", art.getMedium());
    assertEquals("77x53cm", art.getDimensions());
    assertTrue(art.isHasCertificate());

    String info = art.getSpecificInfo();
    assertTrue(info.contains("Da Vinci"));
    assertTrue(info.contains("1503"));
    assertTrue(info.contains("Oil"));
    assertTrue(info.contains("Có chứng chỉ xác thực"));

    // Validation: artist blank
    assertThrows(IllegalArgumentException.class, () -> art.setArtist(""));
    // Validation: yearCreated <= 0
    assertThrows(IllegalArgumentException.class, () -> art.setYearCreated(0));
    // Validation: medium blank
    assertThrows(IllegalArgumentException.class, () -> art.setMedium(" "));
    // Validation: dimensions blank
    assertThrows(IllegalArgumentException.class, () -> art.setDimensions(null));

    art.setHasCertificate(false);
    assertTrue(art.getSpecificInfo().contains("Không có chứng chỉ"));

    assertNotNull(art.toString());
  }

  @Test
  public void testElectronicsSubclass() {
    User seller = createTestSeller();
    Electronics phone =
        new Electronics(
            "iPhone 15",
            "Apple smartphone",
            999.0,
            ItemCondition.NEW,
            "img",
            seller,
            "Apple",
            "15 Pro",
            12);

    assertEquals("Apple", phone.getBrand());
    assertEquals("15 Pro", phone.getModel());
    assertEquals(12, phone.getWarrantyMonths());

    String info = phone.getSpecificInfo();
    assertTrue(info.contains("Apple"));
    assertTrue(info.contains("15 Pro"));
    assertTrue(info.contains("12 tháng"));

    // Validation
    assertThrows(IllegalArgumentException.class, () -> phone.setBrand(""));
    assertThrows(IllegalArgumentException.class, () -> phone.setModel("  "));
    assertThrows(IllegalArgumentException.class, () -> phone.setWarrantyMonths(-1));

    assertNotNull(phone.toString());
  }

  @Test
  public void testVehicleSubclass() {
    User seller = createTestSeller();
    Vehicle car =
        new Vehicle(
            "Tesla Model 3",
            "Electric sedan",
            35000.0,
            ItemCondition.GOOD,
            "img",
            seller,
            "Tesla",
            "Model 3",
            2022,
            15000,
            "Red",
            "Electric");

    assertEquals("Tesla", car.getMake());
    assertEquals("Model 3", car.getModel());
    assertEquals(2022, car.getYear());
    assertEquals(15000, car.getMileageKm());
    assertEquals("Red", car.getColor());
    assertEquals("Electric", car.getFuelType());

    String info = car.getSpecificInfo();
    assertTrue(info.contains("Tesla"));
    assertTrue(info.contains("Model 3"));
    assertTrue(info.contains("2022"));
    assertTrue(info.contains("15000 km"));
    assertTrue(info.contains("Red"));
    assertTrue(info.contains("Electric"));

    // Validation
    assertThrows(IllegalArgumentException.class, () -> car.setMake(""));
    assertThrows(IllegalArgumentException.class, () -> car.setModel(null));
    assertThrows(IllegalArgumentException.class, () -> car.setYear(0));
    assertThrows(IllegalArgumentException.class, () -> car.setMileageKm(-5));
    assertThrows(IllegalArgumentException.class, () -> car.setColor(" "));
    assertThrows(IllegalArgumentException.class, () -> car.setFuelType(""));

    assertNotNull(car.toString());
  }

  @Test
  public void testOtherSubclass() {
    User seller = createTestSeller();
    Other other = new Other("Mug", "Coffee mug", 5.0, ItemCondition.NEW, "img", seller);
    assertEquals("", other.getSpecificInfo());
    assertNotNull(other.toString());
  }
}
