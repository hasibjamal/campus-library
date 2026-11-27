package edu.campus.library.service;

import edu.campus.library.domain.loan.Loan;
import edu.campus.library.infra.persistence.FileRepositoryFactory;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import edu.campus.library.service.eligibility.LoanRefusedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Main use case end to end: borrow, overdue scan (Observer), return with fine (State + Strategy). */
class LoanServiceTest {
    @TempDir
    Path dir;

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
        assertThrows(LoanRefusedException.class, () -> w.loans.borrow("M-2", "B-1"));
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
    void renewalUsesMemberLoanPeriodAndIsLogged() {
        Loan student = w.loans.borrow("M-1", "B-1");
        Loan staff = w.loans.borrow("M-2", "B-2");
        w.date.advanceDays(10);
        assertEquals(w.date.today().plusDays(18), w.loans.renew(student.id()).dueDate());
        assertEquals(w.date.today().plusDays(46), w.loans.renew(staff.id()).dueDate());
        assertTrue(w.activity.lines().get(2).contains("RENEWED loan=" + student.id()));
        w.date.advanceDays(10); // student due date is now day 28, today is day 20: not late
        assertTrue(w.loans.scanOverdue().isEmpty(), "renewed loan is not overdue");
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
    void sameServicesWorkUnchangedOnFileRepositories() {
        LibraryWorld onFile = new LibraryWorld(new FileRepositoryFactory(dir));
        Loan l = onFile.loans.borrow("M-1", "B-1");
        onFile.date.advanceDays(16);
        onFile.loans.returnBook(l.id());

        // a fresh set of services reading the same folder sees the stored state
        LibraryWorld reopened = new LibraryWorld(new FileRepositoryFactory(dir));
        assertEquals("RETURNED", reopened.loans.get(l.id()).stateName());
        assertEquals(new BigDecimal("1.00"), reopened.members.get("M-1").fineBalance());
    }
}
