package edu.campus.library.domain.fine;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Strategy pattern (L8): how a late return is charged. One class per rule,
 * chosen per reader type in the composition root.
 */
@FunctionalInterface
public interface FineStrategy extends Serializable {
    BigDecimal fineFor(long daysLate);
}
