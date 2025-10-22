package Group3.demo.Service;

import Group3.demo.Entity.Promotion;
import Group3.demo.Entity.enums.DiscountType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final Map<DiscountType, DiscountStrategy> strategyMap;

    public DiscountService(List<DiscountStrategy> strategies) {
        // Build a lookup map by DiscountType for fast selection
        // If multiple strategies are registered for the same type, the last one wins
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(DiscountStrategy::getType, s -> s, (a, b) -> b));
    }

    /**
     * Calculate the absolute discount amount for the given promotion and base amount.
     * Returns BigDecimal.ZERO if promotion or config invalid.
     */
    public BigDecimal calculateDiscount(Promotion promo, BigDecimal amount) {
        if (promo == null || promo.getDiscountType() == null) return BigDecimal.ZERO;
        DiscountStrategy s = strategyMap.get(promo.getDiscountType());
        if (s == null) return BigDecimal.ZERO;
        return s.calculate(promo, amount == null ? BigDecimal.ZERO : amount);
    }
}
