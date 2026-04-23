package auction.model.item;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Enum as a Factory Discriminator                            │
 * │                                                                         │
 * │  ItemType is the "switch key" that tells ItemFactory which concrete     │
 * │  Item subclass to instantiate. This keeps factory logic centralised     │
 * │  and makes adding a new item category a localised change.               │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. If you were to add a new item category "CollectibleCard", what     │
 * │      files would you need to change? List every class/enum involved.    │
 * │      This exercise illustrates the "Open/Closed Principle" — open for  │
 * │      extension, closed for modification.                                │
 * │                                                                         │
 * │  Q2. ItemFactory.createItem() uses a switch or if-else on ItemType.     │
 * │      Could you instead store a `Supplier<Item>` or factory lambda       │
 * │      directly inside each enum constant? What would that look like?     │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public enum ItemType {
    ELECTRONICS,
    ART,
    VEHICLE
}
