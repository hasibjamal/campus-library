package edu.campus.library.domain.fine;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Charges a fixed rate per late day after an optional grace period,
 * never more than a cap. Student and staff rules are two configurations.
 */
public class DailyCappedFine implements FineStrategy {
    private static final long serialVersionUID = 1L;

    private final BigDecimal perDay;
    private final int graceDays;
    private final BigDecimal cap;

    public DailyCappedFine(BigDecimal perDay, int graceDays, BigDecimal cap) {
        this.perDay = perDay;
        this.graceDays = graceDays;
        this.cap = cap;
    }

    public static DailyCappedFine student() {
        return new DailyCappedFine(new BigDecimal("0.50"), 0, new BigDecimal("20.00"));
    }

    public static DailyCappedFine staff() {
        return new DailyCappedFine(new BigDecimal("0.25"), 2, new BigDecimal("10.00"));
    }

    @Override
    public BigDecimal fineFor(long daysLate) {
        long charged = Math.max(0, daysLate - graceDays);
        BigDecimal raw = perDay.multiply(BigDecimal.valueOf(charged));
        return raw.min(cap).setScale(2, RoundingMode.HALF_UP);
    }
}
