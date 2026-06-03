package org.tamtamcatworks.auction.service.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.Electronics;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.Other;
import org.tamtamcatworks.auction.model.item.Vehicle;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.ItemRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.mapper.ItemMapper;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ItemMapper itemMapper;

  private ItemService itemService;
  private User seller;

  @BeforeEach
  void setUp() {
    seller = new User("seller", "seller@example.com", "pw", "John Seller", 1000.0);

    List<ItemCreator> creators = List.of(
        new ArtCreator(),
        new ElectronicsCreator(),
        new VehicleCreator(),
        new OtherCreator()
    );

    itemService = new ItemService(itemRepository, userRepository, creators, itemMapper);
    itemService.init(); // manually invoke PostConstruct
  }

  @Test
  void testCreateArtSuccess() {
    when(userRepository.findById("seller123")).thenReturn(Optional.of(seller));
    when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

    Map<String, Object> details = new HashMap<>();
    details.put("artist", "Vincent");
    details.put("yearCreated", 2024);
    details.put("medium", "Oil");
    details.put("dimensions", "50x50");
    details.put("hasCertificate", false);

    Item item = itemService.create(
        "ART", "Starry Painting", "Beautiful art piece",
        200.0, "GOOD", "imgUrl", details, "seller123"
    );

    assertNotNull(item);
    assertTrue(item instanceof Art);
    Art art = (Art) item;
    assertEquals("Vincent", art.getArtist());
    assertEquals(2024, art.getYearCreated());
    assertEquals("Oil", art.getMedium());
    assertFalse(art.isHasCertificate());
  }

  @Test
  void testCreateElectronicsSuccess() {
    when(userRepository.findById("seller123")).thenReturn(Optional.of(seller));
    when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

    Map<String, Object> details = new HashMap<>();
    details.put("brand", "LG");
    details.put("model", "OLED55");
    details.put("warrantyMonths", 24);

    Item item = itemService.create(
        "ELECTRONICS", "LG OLED", "Smart TV",
        1000.0, "NEW", "imgUrl", details, "seller123"
    );

    assertNotNull(item);
    assertTrue(item instanceof Electronics);
    Electronics elec = (Electronics) item;
    assertEquals("LG", elec.getBrand());
    assertEquals("OLED55", elec.getModel());
    assertEquals(24, elec.getWarrantyMonths());
  }

  @Test
  void testCreateVehicleSuccess() {
    when(userRepository.findById("seller123")).thenReturn(Optional.of(seller));
    when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

    Map<String, Object> details = new HashMap<>();
    details.put("make", "Tesla");
    details.put("model", "Model 3");
    details.put("year", 2021);
    details.put("mileageKm", 25000);
    details.put("color", "Red");
    details.put("fuelType", "Electric");

    Item item = itemService.create(
        "VEHICLE", "Tesla 3", "Electric vehicle",
        30000.0, "GOOD", "imgUrl", details, "seller123"
    );

    assertNotNull(item);
    assertTrue(item instanceof Vehicle);
    Vehicle veh = (Vehicle) item;
    assertEquals("Tesla", veh.getMake());
    assertEquals("Model 3", veh.getModel());
    assertEquals(2021, veh.getYear());
    assertEquals(25000, veh.getMileageKm());
    assertEquals("Red", veh.getColor());
    assertEquals("Electric", veh.getFuelType());
  }

  @Test
  void testCreateOtherSuccess() {
    when(userRepository.findById("seller123")).thenReturn(Optional.of(seller));
    when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

    Item item = itemService.create(
        "OTHER", "Rare Comic", "Amazing Fantasy 15",
        500.0, "FAIR", "imgUrl", Map.of(), "seller123"
    );

    assertNotNull(item);
    assertTrue(item instanceof Other);
  }

  @Test
  void testCreateInvalidConditionThrowsException() {
    when(userRepository.findById("seller123")).thenReturn(Optional.of(seller));
    assertThrows(IllegalArgumentException.class, () -> 
        itemService.create(
            "OTHER", "Rare Comic", "Amazing Fantasy 15",
            500.0, "INVALID_CONDITION", "imgUrl", Map.of(), "seller123"
        )
    );
  }
}
