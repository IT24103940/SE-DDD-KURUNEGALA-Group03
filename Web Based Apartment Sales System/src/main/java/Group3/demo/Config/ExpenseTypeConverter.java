package Group3.demo.Config;

import Group3.demo.Entity.enums.ExpenseType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ExpenseTypeConverter implements Converter<String, ExpenseType> {

    @Override
    public ExpenseType convert(@Nullable String source) {
        if (source == null) return null;

        // remove emojis and punctuation, keep letters, digits, underscore and spaces
        String cleaned = source.replaceAll("[^\\p{L}\\p{Nd}_ ]+", "")
                               .trim()
                               .replaceAll("\\s+", "_")
                               .toUpperCase();

        try {
            return ExpenseType.valueOf(cleaned);
        } catch (IllegalArgumentException ex) {
            // fallback: try relaxed matching
            for (ExpenseType t : ExpenseType.values()) {
                if (t.name().equalsIgnoreCase(cleaned)) return t;
                if (t.name().replaceAll("_", "").equalsIgnoreCase(cleaned.replaceAll("_", ""))) return t;
            }
            throw new IllegalArgumentException("Unknown ExpenseType: " + source);
        }
    }
}
