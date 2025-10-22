package Group3.demo.Service;

import Group3.demo.Entity.Promotion;
import java.math.BigDecimal;

import Group3.demo.Entity.enums.DiscountType;

/**
 * Strategy interface for calculating a discount amount for a Promotion.
 */
public interface DiscountStrategy {
    /**
     * The discount type this strategy handles (FIXED or PERCENTAGE).
     */
    DiscountType getType();

    /**
     * Calculate the discount amount for the given promotion and base amount.
     * Returns the absolute discount amount (not the final price).
     */
    BigDecimal calculate(Promotion promotion, BigDecimal amount);
}

