package auction.factory;

import auction.model.item.*;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Factory Method / Simple Factory Pattern                    │
 * │                                                                         │
 * │  ItemFactory is the sole creator of Item subclasses. Client code        │
 * │  never calls `new Electronics(...)` — it always goes through here.      │
 * │  This centralises creation logic and enforces the constraint defined    │
 * │  in the spec.                                                           │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. createItem() uses a vararg `Object... categoryArgs`. This is       │
 * │      flexible but loses compile-time type safety. List two runtime      │
 * │      errors that could occur if the caller passes wrong types or the    │
 * │      wrong number of arguments. How does the factory guard against      │
 * │      each?                                                              │
 * │                                                                         │
 * │  Q2. The factory is a utility class (all-static, never instantiated).  │
 * │      How do you prevent anyone from calling `new ItemFactory()`?        │
 * │      (Hint: private constructor.)                                       │
 * │                                                                         │
 * │  Q3. As a refactoring exercise, rewrite createItem() using a           │
 * │      switch expression (Java 14+) instead of if-else. How does the     │
 * │      switch expression improve exhaustiveness checking?                 │
 * │                                                                         │
 * │  📌 SPRING POINTER                                                      │
 * │  In a Spring context, ItemFactory could become a @Service bean and     │
 * │  createItem() a regular method. Spring could then inject it wherever   │
 * │  needed rather than using static calls. This makes it easier to mock   │
 * │  in tests with @MockBean.                                              │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class ItemFactory {

    /** Utility class — no instantiation allowed. */
    private ItemFactory() {}

    /**
     * Creates and returns the correct concrete Item subclass.
     *
     * categoryArgs order per type:
     *   ELECTRONICS : (String brand, int warrantyMonths, String modelNumber)
     *   ART         : (String artist, int yearCreated, String medium, boolean isCertified)
     *   VEHICLE     : (String make, String model, int year, int mileage)
     *
     * @throws IllegalArgumentException if type is null, basePrice <= 0,
     *                                  or categoryArgs count/types are wrong.
     */
    public static Item createItem(
            ItemType      type,
            String        name,
            String        description,
            double        basePrice,
            ItemCondition condition,
            Object...     categoryArgs) {

        // TODO: validate type != null, basePrice > 0
        // TODO: switch/if-else on type:
        //         ELECTRONICS → cast args, construct Electronics
        //         ART         → cast args, construct Art
        //         VEHICLE     → cast args, construct Vehicle
        //         default     → throw IllegalArgumentException("Unknown ItemType: " + type)
        // TODO: wrap ClassCastException / ArrayIndexOutOfBoundsException in IllegalArgumentException

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
