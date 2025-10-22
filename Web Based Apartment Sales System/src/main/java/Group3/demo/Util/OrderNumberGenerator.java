package Group3.demo.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enum-based singleton for generating invoice/order numbers application-wide.
 * Use OrderNumberGenerator.INSTANCE.generate(orderId) or generate(orderId, suffix).
 */
public enum OrderNumberGenerator {
    INSTANCE;

    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() & 0xfffffff);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String generate(Integer orderId) {
        String ts = TS_FMT.format(LocalDateTime.now());
        long seq = counter.incrementAndGet();
        return "INV-" + ts + "-SO" + orderId + "-" + seq;
    }

    public String generate(Integer orderId, String suffix) {
        String ts = TS_FMT.format(LocalDateTime.now());
        long seq = counter.incrementAndGet();
        return "INV-" + ts + "-SO" + orderId + "-" + suffix + "-" + seq;
    }
}

