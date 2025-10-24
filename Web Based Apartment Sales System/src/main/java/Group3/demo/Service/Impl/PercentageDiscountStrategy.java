package Group3.demo.Service.Impl;

import Group3.demo.Service.DiscountStrategy;
import Group3.demo.Entity.Promotion;
import Group3.demo.Entity.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType getType() {
        return DiscountType.PERCENT;
    }

    @Override
    public BigDecimal calculate(Promotion promotion, BigDecimal amount) {
        if (promotion == null || promotion.getDiscountValue() == null || amount == null) return BigDecimal.ZERO;
        BigDecimal pct = promotion.getDiscountValue();
        if (pct.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return amount.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}

