package auction.model.item;

import java.time.LocalDate;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Concrete Subclass & Override                               │
 * │                                                                         │
 * │  Electronics provides category-specific fields and overrides the two    │
 * │  abstract methods from Item with Electronics-specific logic.            │
 * │                                                                         │
 * │  estimatedValue() formula (from spec):                                  │
 * │    basePrice × conditionMultiplier + (warrantyMonths × 5.0)            │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. The constructor is package-private (no `public` modifier).         │
 * │      Try making it public and see what the spec says about factory      │
 * │      enforcement. Then explain how package-private visibility prevents  │
 * │      direct instantiation from outside the `auction.model.item`         │
 * │      package.                                                           │
 * │                                                                         │
 * │  Q2. warrantyMonths = 0 means "no warranty". Is this a good sentinel   │
 * │      value? What alternative design (e.g. Optional<Integer>) could      │
 * │      make the "no warranty" case more explicit?                         │
 * │                                                                         │
 * │  Q3. Write the estimatedValue() formula step by step. If warrantyMonths │
 * │      is 0 and condition is POOR, what is the estimated value of an item │
 * │      with basePrice = 1000.0?                                           │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class Electronics extends Item {

    // TODO: declare brand          (String)
    // TODO: declare warrantyMonths (int, 0 = no warranty)
    // TODO: declare modelNumber    (String)

    /**
     * Package-private — only ItemFactory may call this.
     */
    Electronics(String name, String description, double basePrice,
                ItemCondition condition,
                String brand, int warrantyMonths, String modelNumber) {
        super(name, description, basePrice, condition);
        // TODO: assign brand, warrantyMonths, modelNumber
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getBrand()          { throw new UnsupportedOperationException("Not yet implemented"); }
    public int    getWarrantyMonths() { throw new UnsupportedOperationException("Not yet implemented"); }
    public String getModelNumber()    { throw new UnsupportedOperationException("Not yet implemented"); }

    /**
     * Formula: basePrice × conditionMultiplier + (warrantyMonths × 5.0)
     * Condition multipliers: NEW=1.0, LIKE_NEW=0.9, GOOD=0.75, FAIR=0.55, POOR=0.35
     */
    @Override
    public double estimatedValue() {
        // TODO: implement formula
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Example output: "Brand: Asus | Warranty: 12 months | Model: ROG-G15"
     */
    @Override
    public String getCategoryDetails() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getDisplayInfo() {
        // TODO: combine Item fields and category details into a readable string
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
