package auction.model.item;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Enums as Type-Safe Constants with Behaviour                │
 * │                                                                         │
 * │  ItemCondition replaces fragile String/int constants with a proper      │
 * │  type. Java enums can carry fields and methods, making them ideal for   │
 * │  mapping condition → multiplier in one place.                           │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. Add a `multiplier` field to each constant so that                  │
 * │      Electronics.estimatedValue() can call                              │
 * │      `condition.getMultiplier()` instead of a switch statement.         │
 * │      What are the advantages of putting the multiplier in the enum?     │
 * │                                                                         │
 * │  Q2. What happens at runtime if you call                                │
 * │      `ItemCondition.valueOf("MINT")`? How would you guard against       │
 * │      this when parsing user input from a JavaFX TextField?              │
 * │                                                                         │
 * │  Q3. Why is using an enum safer than using a String or int constant     │
 * │      to represent condition? Give a concrete example of a bug that      │
 * │      would be caught at compile time with an enum but not with a String.│
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public enum ItemCondition {

    // Condition multipliers for Electronics.estimatedValue():
    // NEW=1.0, LIKE_NEW=0.9, GOOD=0.75, FAIR=0.55, POOR=0.35

    NEW,       // Brand new, unopened
    LIKE_NEW,  // Opened but barely used
    GOOD,      // Normal use, minor wear
    FAIR,      // Visible wear, fully functional
    POOR;      // Heavy wear, may have defects

    // TODO (Q1): Add a `final double multiplier` field and constructor,
    //            then pass the correct multiplier to each constant above.

    // TODO: Add getMultiplier() getter.
}
