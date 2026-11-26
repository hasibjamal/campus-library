package edu.campus.library.service;

import edu.campus.library.domain.loan.Loan;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Main use case end to end: borrow, overdue scan (Observer), return with fine (State + Strategy). */
class LoanServiceTest {
    private LibraryWorld w;

    @BeforeEach
    void setUp() {
        w = new LibraryWorld(new InMemoryRepositoryFactory());
    }

    @Test
    void borrowUsesLoanPeriodOfMemberType() {
        Loan student = w.loans.borrow("M-1", "B-1");
        Loan staff = w.loans.borrow("M-2", "B-2");
        assertEquals(w.date.today().plusDays(14), student.dueDate());
        assertEquals(w.date.today().plusDays(28), staff.dueDate());
        assertFalse(w.loans.isAvailable("B-1"));
    }

    @Test
    void sameBookCannotBeLentTwice() {
        w.loans.borrow("M-1", "B-1");
        assertThrows(IllegalStateException.class, () -> w.loans.borrow("M-2", "B-1"));
    }

    @Test
    void reserveThenPickUp() {
        Loan l = w.loans.reserve("M-1", "B-3");
        assertEquals("RESERVED", l.stateName());
        assertFalse(w.loans.isAvailable("B-3"));
        assertEquals("BORROWED", w.loans.pickUp(l.id()).stateName());
    }

    @Test
    void cancelledReservationFreesTheBook() {
        Loan l = w.loans.reserve("M-1", "B-3");
        w.loans.cancelReservation(l.id());
        assertTrue(w.loans.isAvailable("B-3"));
    }

    @Test
    void overdueScanFlagsLoansAndSendsNotices() {
        Loan l = w.loans.borrow("M-1", "B-1");
        w.loans.borrow("M-2", "B-2");
        w.date.advanceDays(20);
        List<Loan> flagged = w.loans.scanOverdue();
        assertEquals(List.of(l.id()), flagged.stream().map(Loan::id).toList());
        assertEquals("OVERDUE", w.loans.get(l.id()).stateName());
        assertEquals(1, w.channel.sent.size());
        assertTrue(w.channel.sent.get(0).startsWith("M-1|Overdue notice"));
        assertTrue(w.loans.scanOverdue().isEmpty(), "second scan does not notify again");
    }

    @Test
    void lateReturnChargesFineToMemberAndNotifies() {
        Loan l = w.loans.borrow("M-1", "B-1");
        w.date.advanceDays(20); // 6 days late
        assertEquals(new BigDecimal("3.00"), w.loans.previewFine(l.id()));
        Loan returned = w.loans.returnBook(l.id());
        assertEquals("RETURNED", returned.stateName());
        assertEquals(new BigDecimal("3.00"), returned.fine());
        assertEquals(new BigDecimal("3.00"), w.members.get("M-1").fineBalance());
        assertTrue(w.channel.sent.get(0).contains("Fine charged"));
        assertTrue(w.loans.isAvailable("B-1"));
    }

    @Test
    void onTimeReturnChargesNothing() {
        Loan l = w.loans.borrow("M-1", "B-1");
        w.date.advanceDays(3);
        w.loans.returnBook(l.id());
        assertEquals(new BigDecimal("0.00"), w.members.get("M-1").fineBalance());
        assertTrue(w.channel.sent.isEmpty());
    }

    @Test
    void activityLogRecordsEveryEvent() {
        Loan l = w.loans.borrow("M-1", "B-1");
        w.loans.returnBook(l.id());
        assertEquals(2, w.activity.lines().size());
        assertTrue(w.activity.lines().get(0).contains("BORROWED"));
        assertTrue(w.activity.lines().get(1).contains("RETURNED"));
    }

    @Test
    void activeLoansOfMember() {
        w.loans.borrow("M-1", "B-1");
        w.loans.reserve("M-1", "B-2");
        Loan done = w.loans.borrow("M-1", "B-3");
        w.loans.returnBook(done.id());
        assertEquals(2, w.loans.activeLoansOf("M-1").size());
        assertEquals(3, w.loans.all().size());
    }

    @Test
    void unknownIdsAreReported() {
        assertThrows(NoSuchElementException.class, () -> w.loans.get("L-404"));
        assertThrows(NoSuchElementException.class, () -> w.loans.borrow("M-404", "B-1"));
        assertThrows(NoSuchElementException.class, () -> w.loans.borrow("M-1", "B-404"));
    }

    @Test
    void loanRulesAreEnforced() {
        w.members.get("M-1").addFine(new BigDecimal("6.00"));
        assertTrue(assertThrows(IllegalStateException.class, () -> w.loans.borrow("M-1", "B-1"))
                .getMessage().contains("Unpaid fines"));
        w.loans.borrow("M-2", "B-1");
        w.members.get("M-2").deactivate();
        assertTrue(assertThrows(IllegalStateException.class, () -> w.loans.borrow("M-2", "B-2"))
                .getMessage().contains("not active"));
    }

    @Test
    void studentLoanLimitIsThree() {
        w.loans.borrow("M-1", "B-1");
        w.loans.borrow("M-1", "B-2");
        w.loans.borrow("M-1", "B-3");
        assertTrue(assertThrows(IllegalStateException.class, () -> w.loans.borrow("M-1", "B-4"))
                .getMessage().contains("limit"));
    }
}
