package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.event.LoanEvent;
import edu.campus.library.domain.event.LoanEventType;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.loan.LoanStatus;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/** Main use case: reserve, borrow and return books; detect overdue loans. */
public class LoanService {
    private static final BigDecimal FINE_LIMIT = new BigDecimal("5.00");

    private final Repository<Loan> loans;
    private final MemberService members;
    private final CatalogService catalog;
    private final LoanEvents events;
    private final DateProvider date;
    private final IdGenerator ids;

    public LoanService(Repository<Loan> loans, MemberService members, CatalogService catalog,
                       LoanEvents events, DateProvider date, IdGenerator ids) {
        this.loans = loans;
        this.members = members;
        this.catalog = catalog;
        this.events = events;
        this.date = date;
        this.ids = ids;
    }

    public Loan reserve(String memberId, String bookId) {
        Loan loan = openLoan(memberId, bookId);
        publish(LoanEventType.RESERVED, loan);
        return loan;
    }

    public Loan borrow(String memberId, String bookId) {
        Loan loan = openLoan(memberId, bookId);
        return pickUp(loan.id());
    }

    public Loan pickUp(String loanId) {
        Loan loan = get(loanId);
        Member member = members.get(loan.memberId());
        loan.pickUp(date.today(), member.type().loanDays());
        loans.save(loan);
        publish(LoanEventType.BORROWED, loan);
        return loan;
    }

    public Loan cancelReservation(String loanId) {
        Loan loan = get(loanId);
        loan.cancel();
        return loans.save(loan);
    }

    public Loan returnBook(String loanId) {
        Loan loan = get(loanId);
        Member member = members.get(loan.memberId());
        BigDecimal fine = fineFor(member.type(), loan.daysLate(date.today()));
        loan.returnItem(date.today(), fine);
        loans.save(loan);
        publish(LoanEventType.RETURNED, loan);
        if (fine.signum() > 0) {
            member.addFine(fine);
            members.save(member);
            events.publish(new LoanEvent(LoanEventType.FINE_CHARGED, loan.id(), loan.memberId(),
                    loan.bookId(), date.today(), fine));
        }
        return loan;
    }

    /** Fine rules per reader type. */
    BigDecimal fineFor(MemberType type, long daysLate) {
        BigDecimal amount;
        if (type == MemberType.STUDENT) {
            amount = new BigDecimal("0.50").multiply(BigDecimal.valueOf(Math.max(0, daysLate)))
                    .min(new BigDecimal("20.00"));
        } else {
            amount = new BigDecimal("0.25").multiply(BigDecimal.valueOf(Math.max(0, daysLate - 2)))
                    .min(new BigDecimal("10.00"));
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Loan> scanOverdue() {
        List<Loan> flagged = new ArrayList<>();
        for (Loan loan : loans.findAll()) {
            if (loan.status() == LoanStatus.BORROWED && loan.daysLate(date.today()) > 0) {
                loan.markOverdue(date.today());
                loans.save(loan);
                publish(LoanEventType.OVERDUE, loan);
                flagged.add(loan);
            }
        }
        return flagged;
    }

    public boolean isAvailable(String bookId) {
        return loans.findAll().stream().noneMatch(l -> l.bookId().equals(bookId) && l.holdsBook());
    }

    public List<Loan> activeLoansOf(String memberId) {
        return loans.findAll().stream()
                .filter(l -> l.memberId().equals(memberId) && l.holdsBook())
                .toList();
    }

    public List<Loan> all() {
        return loans.findAll();
    }

    public Loan get(String loanId) {
        return loans.findById(loanId).orElseThrow(() -> new NoSuchElementException("No loan " + loanId));
    }

    private Loan openLoan(String memberId, String bookId) {
        Member member = members.get(memberId);
        Book book = catalog.getBook(bookId);
        if (!member.isActive()) {
            throw new IllegalStateException("Membership of " + member.id() + " is not active");
        }
        if (member.fineBalance().compareTo(FINE_LIMIT) > 0) {
            throw new IllegalStateException("Unpaid fines " + member.fineBalance()
                    + " exceed the limit of " + FINE_LIMIT);
        }
        if (activeLoansOf(memberId).size() >= member.type().maxLoans()) {
            throw new IllegalStateException("Loan limit of " + member.type().maxLoans() + " reached");
        }
        if (!isAvailable(bookId)) {
            throw new IllegalStateException("Book " + book.id() + " is already on loan or reserved");
        }
        Loan loan = new Loan(ids.next("L"), bookId, memberId, date.today());
        return loans.save(loan);
    }

    private void publish(LoanEventType type, Loan loan) {
        events.publish(LoanEvent.of(type, loan.id(), loan.memberId(), loan.bookId(), date.today()));
    }
}
