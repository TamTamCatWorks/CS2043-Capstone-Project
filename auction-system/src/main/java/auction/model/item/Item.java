package auction.model.item;

import auction.model.Entity;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Abstract Classes with Abstract Methods (Polymorphism)      │
 * │                                                                         │
 * │  Item defines the shared fields of every auction item and declares two  │
 * │  abstract methods that force each subclass to provide its own logic.    │
 * │  This is the Template Method / polymorphism pattern in action.          │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. getCategoryDetails() and estimatedValue() are declared abstract.   │
 * │      Could they have been left as concrete methods with a default        │
 * │      body? What would a sensible default even look like, and why is     │
 * │      "no default" a better design here?                                 │
 * │                                                                         │
 * │  Q2. The spec says Item instances must be created through ItemFactory   │
 * │      only — never via `new Electronics(...)` in client code. How can    │
 * │      you enforce this? (Hint: think about constructor visibility —       │
 * │      what access modifier should subclass constructors have?)           │
 * │                                                                         │
 * │  Q3. ItemCondition is an enum used as a field here. Look at the         │
 * │      condition multipliers in Electronics.estimatedValue(). How would   │
 * │      you store those multipliers so they are defined exactly once       │
 * │      and shared across all subclasses? (Hint: enum fields or a map.)   │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public abstract class Item extends Entity {

    // TODO: declare name        (String, immutable)
    // TODO: declare description (String, immutable)
    // TODO: declare basePrice   (double, > 0, immutable)
    // TODO: declare condition   (ItemCondition, immutable)

    /**
     * Package-private constructor — only ItemFactory (same package or via
     * subclass constructors) should be able to call this.
     */
    protected Item(String name, String description, double basePrice, ItemCondition condition) {
        super();
        // TODO: validate basePrice > 0; validate non-null fields; assign all
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String        getName()        { throw new UnsupportedOperationException("Not yet implemented"); }
    public String        getDescription() { throw new UnsupportedOperationException("Not yet implemented"); }
    public double        getBasePrice()   { throw new UnsupportedOperationException("Not yet implemented"); }
    public ItemCondition getCondition()   { throw new UnsupportedOperationException("Not yet implemented"); }

    /**
     * Returns a formatted, human-readable summary of category-specific fields.
     * Used by the JavaFX detail view to populate item cards.
     *
     * Example for Electronics: "Brand: Asus | Warranty: 12 months | Model: ROG-G15"
     */
    public abstract String getCategoryDetails();

    /**
     * Returns an estimated market value based on condition and category factors.
     * Each subclass implements its own formula (see spec for each formula).
     */
    public abstract double estimatedValue();
}
