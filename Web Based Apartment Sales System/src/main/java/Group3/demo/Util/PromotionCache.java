package Group3.demo.Util;

import Group3.demo.Entity.Promotion;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example of a classic singleton using the enum pattern.
 * <p>
 * This cache stores Promotion objects in-memory keyed by their id.
 * It is thread-safe and safe for serialization (enum singletons are the recommended approach).
 *
 * Usage examples:
 *   PromotionCache.INSTANCE.put(promotion);
 *   Promotion cached = PromotionCache.INSTANCE.get(id);
 */
public enum PromotionCache {
    INSTANCE;

    private final Map<Integer, Promotion> cache = new ConcurrentHashMap<>();

    public Promotion get(Integer id) {
        if (id == null) return null;
        return cache.get(id);
    }

    public void put(Promotion promotion) {
        if (promotion == null || promotion.getId() == null) return;
        cache.put(promotion.getId(), promotion);
    }

    public void remove(Integer id) {
        if (id == null) return;
        cache.remove(id);
    }

    public void clear() {
        cache.clear();
    }

    public Collection<Promotion> all() {
        return cache.values();
    }

    public int size() {
        return cache.size();
    }
}

