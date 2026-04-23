package auction.model.item;

import java.time.LocalDate;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Compound Depreciation Formula & Math Utilities             │
 * │                                                                         │
 * │  estimatedValue() formula (from spec):                                  │
 * │    basePrice × ageFactor × mileageFactor                               │
 * │    ageFactor     = Math.max(0.1,  1 - (currentYear - year) × 0.05)    │
 * │    mileageFactor = Math.max(0.2,  1 - mileage / 500_000.0)            │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. Math.max(0.1, ...) ensures ageFactor never drops below 0.1.       │
 * │      After how many years does a vehicle hit this floor? Show the       │
 * │      calculation. What real-world assumption does this floor represent? │
 * │                                                                         │
 * │  Q2. mileage is in km. How would you add a unit-conversion feature      │
 * │      for miles input without changing the internal formula? (Consider   │
 * │      a static factory method or a builder parameter.)                   │
 * │                                                                         │
 * │  Q3. Vehicle has both `make` and `model` as String fields. Could        │
 * │      `make` be an enum instead (e.g. VehicleMake.HONDA)? What are      │
 * │      the tradeoffs of using an enum vs a free String for make?         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class Vehicle extends Item {

    // TODO: declare make    (String, e.g. "Honda")
    // TODO: declare model   (String, e.g. "Civic")
    // TODO: declare year    (int, model year)
    // TODO: declare mileage (int, odometer in km)

    /**
     * Package-private — only ItemFactory may call this.
     */
    Vehicle(String name, String description, double basePrice,
            ItemCondition condition,
            String make, String model, int year, int mileage) {
        super(name, description, basePrice, condition);
        // TODO: assign fields
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getMake()    { throw new UnsupportedOperationException("Not yet implemented"); }
    public String getModel()   { throw new UnsupportedOperationException("Not yet implemented"); }
    public int    getYear()    { throw new UnsupportedOperationException("Not yet implemented"); }
    public int    getMileage() { throw new UnsupportedOperationException("Not yet implemented"); }

    /**
     * Formula: basePrice × ageFactor × mileageFactor
     *   ageFactor     = Math.max(0.1, 1 - (currentYear - year) * 0.05)
     *   mileageFactor = Math.max(0.2, 1 - mileage / 500_000.0)
     */
    @Override
    public double estimatedValue() {
        // TODO: get current year, compute both factors, apply formula
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Example output: "Make: Honda | Model: Civic | Year: 2018 | Mileage: 62000 km"
     */
    @Override
    public String getCategoryDetails() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getDisplayInfo() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
