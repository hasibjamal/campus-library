package edu.campus.library.domain;

import edu.campus.library.domain.fine.DailyCappedFine;
import edu.campus.library.domain.fine.FineStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Strategy pattern: each fine rule is tested alone. */
class FineStrategyTest {

    @Test
    void studentPaysFiftyCentsPerDay() {
        FineStrategy s = DailyCappedFine.student();
        assertEquals(new BigDecimal("0.00"), s.fineFor(0));
        assertEquals(new BigDecimal("0.50"), s.fineFor(1));
        assertEquals(new BigDecimal("5.00"), s.fineFor(10));
    }

    @Test
    void studentFineIsCapped() {
        assertEquals(new BigDecimal("20.00"), DailyCappedFine.student().fineFor(365));
    }

    @Test
    void staffHaveTwoGraceDaysAndLowerRate() {
        FineStrategy s = DailyCappedFine.staff();
        assertEquals(new BigDecimal("0.00"), s.fineFor(2));
        assertEquals(new BigDecimal("0.25"), s.fineFor(3));
        assertEquals(new BigDecimal("10.00"), s.fineFor(500));
    }

    @Test
    void negativeDaysAreTreatedAsOnTime() {
        assertEquals(new BigDecimal("0.00"), DailyCappedFine.student().fineFor(-3));
    }
}
