package Group3.demo.Service.Impl;

import Group3.demo.Service.DiscountStrategy;
import Group3.demo.Entity.Promotion;
import Group3.demo.Entity.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType getType() {
        return DiscountType.FIXED;
    }

    @Override
    public BigDecimal calculate(Promotion promotion, BigDecimal amount) {
        if (promotion == null || promotion.getDiscountValue() == null) return BigDecimal.ZERO;
        return promotion.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
    }
}

