package edu.campus.library.domain;

import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.loan.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static edu.campus.library.TestFixtures.DAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanTest {
    private static final BigDecimal NONE = new BigDecimal("0.00");
    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = new Loan("L-1", "B-1", "M-1", DAY);
    }

    @Test
    void lifecycleReservedBorrowedReturned() {
        assertEquals(LoanStatus.RESERVED, loan.status());
        assertTrue(loan.holdsBook());
        loan.pickUp(DAY, 14);
        assertEquals(LoanStatus.BORROWED, loan.status());
        assertEquals(DAY.plusDays(14), loan.dueDate());
        loan.returnItem(DAY.plusDays(18), new BigDecimal("2.00"));
        assertEquals("RETURNED", loan.stateName());
        assertEquals(new BigDecimal("2.00"), loan.fine());
        assertEquals(4, loan.daysLate(DAY.plusDays(18)));
        assertFalse(loan.holdsBook());
    }

    @Test
    void overdueThenReturned() {
        loan.pickUp(DAY, 14);
        assertThrows(IllegalStateException.class, () -> loan.markOverdue(DAY.plusDays(14)));
        loan.markOverdue(DAY.plusDays(15));
        assertEquals(LoanStatus.OVERDUE, loan.status());
        loan.returnItem(DAY.plusDays(20), NONE);
        assertEquals(LoanStatus.RETURNED, loan.status());
    }

    @Test
    void cancelOnlyWhileReserved() {
        loan.cancel();
        assertEquals(LoanStatus.CANCELLED, loan.status());
        assertThrows(IllegalStateException.class, () -> loan.pickUp(DAY, 14));
    }

    @Test
    void illegalTransitionsAreRejected() {
        assertThrows(IllegalStateException.class, () -> loan.returnItem(DAY, NONE));
        assertThrows(IllegalStateException.class, () -> loan.markOverdue(DAY));
        loan.pickUp(DAY, 14);
        assertThrows(IllegalStateException.class, () -> loan.pickUp(DAY, 14));
        assertThrows(IllegalStateException.class, loan::cancel);
        loan.returnItem(DAY, NONE);
        assertThrows(IllegalStateException.class, () -> loan.returnItem(DAY, NONE));
        assertEquals(0, new Loan("L-2", "B", "M", DAY).daysLate(DAY.plusDays(99)));
    }
}
