package edu.campus.library.domain;

import edu.campus.library.domain.fine.DailyCappedFine;
import edu.campus.library.domain.fine.FineStrategy;
import edu.campus.library.domain.loan.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static edu.campus.library.TestFixtures.DAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** State pattern: legal and illegal loan transitions. */
class LoanStateTest {
    private final FineStrategy fines = DailyCappedFine.student();
    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = new Loan("L-1", "B-1", "M-1", DAY);
    }

    @Test
    void newLoanIsReservedAndHoldsTheBook() {
        assertEquals("RESERVED", loan.stateName());
        assertTrue(loan.holdsBook());
    }

    @Test
    void pickUpSetsDueDateAndMovesToBorrowed() {
        loan.pickUp(DAY, 14);
        assertEquals("BORROWED", loan.stateName());
        assertEquals(DAY, loan.borrowedOn());
        assertEquals(DAY.plusDays(14), loan.dueDate());
    }

    @Test
    void onTimeReturnHasNoFine() {
        loan.pickUp(DAY, 14);
        loan.returnItem(DAY.plusDays(14), fines);
        assertEquals("RETURNED", loan.stateName());
        assertEquals(new BigDecimal("0.00"), loan.fine());
        assertFalse(loan.holdsBook());
    }

    @Test
    void lateReturnFromBorrowedChargesFineEvenWithoutOverdueScan() {
        loan.pickUp(DAY, 14);
        loan.returnItem(DAY.plusDays(18), fines);
        assertEquals(new BigDecimal("2.00"), loan.fine());
        assertEquals(DAY.plusDays(18), loan.returnedOn());
    }

    @Test
    void overdueLoanCanStillBeReturned() {
        loan.pickUp(DAY, 14);
        loan.markOverdue(DAY.plusDays(15));
        assertEquals("OVERDUE", loan.stateName());
        assertTrue(loan.holdsBook());
        loan.returnItem(DAY.plusDays(20), fines);
        assertEquals("RETURNED", loan.stateName());
        assertEquals(new BigDecimal("3.00"), loan.fine());
    }

    @Test
    void cannotMarkOverdueBeforeDueDate() {
        loan.pickUp(DAY, 14);
        assertThrows(IllegalStateException.class, () -> loan.markOverdue(DAY.plusDays(14)));
    }

    @Test
    void reservationCanBeCancelled() {
        loan.cancel();
        assertEquals("CANCELLED", loan.stateName());
        assertFalse(loan.holdsBook());
    }

    @Test
    void illegalTransitionsAreRejected() {
        assertThrows(IllegalStateException.class, () -> loan.returnItem(DAY, fines));
        assertThrows(IllegalStateException.class, () -> loan.markOverdue(DAY));
        loan.pickUp(DAY, 14);
        assertThrows(IllegalStateException.class, () -> loan.pickUp(DAY, 14));
        assertThrows(IllegalStateException.class, loan::cancel);
        loan.returnItem(DAY, fines);
        assertThrows(IllegalStateException.class, () -> loan.returnItem(DAY, fines));
        assertThrows(IllegalStateException.class, () -> loan.markOverdue(DAY.plusDays(30)));
    }

    @Test
    void cancelledReservationCannotBePickedUp() {
        loan.cancel();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> loan.pickUp(DAY, 14));
        assertTrue(e.getMessage().contains("CANCELLED"));
    }

    @Test
    void borrowedLoanCanBeRenewedOnce() {
        loan.pickUp(DAY, 14);
        loan.renew(DAY.plusDays(10), 14);
        assertEquals("BORROWED", loan.stateName());
        assertEquals(DAY.plusDays(28), loan.dueDate());
        assertEquals(1, loan.renewals());
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> loan.renew(DAY.plusDays(20), 14));
        assertTrue(e.getMessage().contains("already renewed"));
    }

    @Test
    void renewalOnTheDueDateIsAllowedButNotAfter() {
        loan.pickUp(DAY, 14);
        assertThrows(IllegalStateException.class, () -> loan.renew(DAY.plusDays(15), 14));
        loan.renew(DAY.plusDays(14), 14);
        assertEquals(DAY.plusDays(28), loan.dueDate());
    }

    @Test
    void renewalIsRefusedInOtherStates() {
        assertThrows(IllegalStateException.class, () -> loan.renew(DAY, 14));
        loan.pickUp(DAY, 14);
        loan.markOverdue(DAY.plusDays(15));
        assertTrue(assertThrows(IllegalStateException.class, () -> loan.renew(DAY.plusDays(15), 14))
                .getMessage().contains("OVERDUE"));
        loan.returnItem(DAY.plusDays(15), fines);
        assertThrows(IllegalStateException.class, () -> loan.renew(DAY.plusDays(15), 14));
    }

    @Test
    void daysLateIsZeroBeforePickUp() {
        assertEquals(0, loan.daysLate(DAY.plusDays(100)));
    }
}
