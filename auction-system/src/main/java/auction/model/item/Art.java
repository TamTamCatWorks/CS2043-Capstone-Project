package auction.model.item;

import java.time.LocalDate;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Overriding with Domain-Specific Valuation Logic            │
 * │                                                                         │
 * │  estimatedValue() formula (from spec):                                  │
 * │    basePrice × ageFactor × (isCertified ? 1.5 : 1.0)                  │
 * │    where ageFactor = 1 + (currentYear - yearCreated) / 100.0           │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. ageFactor grows as the art ages. What would happen to the          │
 * │      formula if yearCreated is in the future (e.g. a 2030 digital       │
 * │      print)? Should the factory validate yearCreated? If so, what       │
 * │      exception should it throw and with what message?                   │
 * │                                                                         │
 * │  Q2. isCertified is a boolean that multiplies the value by 1.5.        │
 * │      Rewrite the conditional expression using a ternary operator and    │
 * │      then using a simple arithmetic trick (no ternary). Which is        │
 * │      more readable?                                                     │
 * │                                                                         │
 * │  Q3. Art does NOT have a condition multiplier in its formula, unlike    │
 * │      Electronics. Is that an oversight or an intentional design choice? │
 * │      Argue both sides.                                                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class Art extends Item {

    // TODO: declare artist      (String)
    // TODO: declare yearCreated (int)
    // TODO: declare medium      (String, e.g. "Oil on canvas")
    // TODO: declare isCertified (boolean)

    /**
     * Package-private — only ItemFactory may call this.
     */
    Art(String name, String description, double basePrice,
        ItemCondition condition,
        String artist, int yearCreated, String medium, boolean isCertified) {
        super(name, description, basePrice, condition);
        // TODO: assign fields
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String  getArtist()      { throw new UnsupportedOperationException("Not yet implemented"); }
    public int     getYearCreated() { throw new UnsupportedOperationException("Not yet implemented"); }
    public String  getMedium()      { throw new UnsupportedOperationException("Not yet implemented"); }
    public boolean isCertified()    { throw new UnsupportedOperationException("Not yet implemented"); }

    /**
     * Formula: basePrice × ageFactor × (isCertified ? 1.5 : 1.0)
     * ageFactor = 1 + (currentYear - yearCreated) / 100.0
     */
    @Override
    public double estimatedValue() {
        // TODO: get current year, compute ageFactor, apply formula
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Example output: "Artist: Mai Thu | Year: 1972 | Medium: Oil on canvas | Certified: Yes"
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
