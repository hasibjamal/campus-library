package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.event.LoanEvent;
import edu.campus.library.domain.event.LoanEventType;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.service.eligibility.EligibilityCheck;
import edu.campus.library.service.eligibility.LoanRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/** Main use case: reserve, borrow and return books; detect overdue loans. */
public class LoanService {
    private final Repository<Loan> loans;
    private final MemberService members;
    private final CatalogService catalog;
    private final EligibilityCheck eligibility;
    private final FinePolicy finePolicy;
    private final LoanEvents events;
    private final SimulatedDate date;
    private final IdGenerator ids;

    public LoanService(Repository<Loan> loans, MemberService members, CatalogService catalog,
                       EligibilityCheck eligibility, FinePolicy finePolicy, LoanEvents events,
                       SimulatedDate date, IdGenerator ids) {
        this.loans = loans;
        this.members = members;
        this.catalog = catalog;
        this.eligibility = eligibility;
        this.finePolicy = finePolicy;
        this.events = events;
        this.date = date;
        this.ids = ids;
    }

    /** Places a hold on a book; the reader collects it later with {@link #pickUp}. */
    public Loan reserve(String memberId, String bookId) {
        Loan loan = openLoan(memberId, bookId);
        publish(LoanEventType.RESERVED, loan);
        return loan;
    }

    /** Reserve and collect in one step (reader is at the desk). */
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

    /** Extends a borrowed loan by the reader's loan period (once, and only while not late). */
    public Loan renew(String loanId) {
        Loan loan = get(loanId);
        Member member = members.get(loan.memberId());
        loan.renew(date.today(), member.type().loanDays());
        loans.save(loan);
        publish(LoanEventType.RENEWED, loan);
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
        loan.returnItem(date.today(), finePolicy.strategyFor(member.type()));
        loans.save(loan);
        publish(LoanEventType.RETURNED, loan);
        if (loan.fine().signum() > 0) {
            member.addFine(loan.fine());
            members.save(member);
            events.publish(new LoanEvent(LoanEventType.FINE_CHARGED, loan.id(), loan.memberId(),
                    loan.bookId(), date.today(), loan.fine()));
        }
        return loan;
    }

    /** Moves every borrowed loan past its due date to OVERDUE and notifies. */
    public List<Loan> scanOverdue() {
        List<Loan> flagged = new ArrayList<>();
        for (Loan loan : loans.findAll()) {
            if ("BORROWED".equals(loan.stateName()) && loan.daysLate(date.today()) > 0) {
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

    public BigDecimal previewFine(String loanId) {
        Loan loan = get(loanId);
        Member member = members.get(loan.memberId());
        return finePolicy.strategyFor(member.type()).fineFor(loan.daysLate(date.today()));
    }

    private Loan openLoan(String memberId, String bookId) {
        Member member = members.get(memberId);
        Book book = catalog.getBook(bookId);
        eligibility.check(new LoanRequest(member, book, activeLoansOf(memberId).size(), isAvailable(bookId)));
        Loan loan = new Loan(ids.next("L"), bookId, memberId, date.today());
        return loans.save(loan);
    }

    private void publish(LoanEventType type, Loan loan) {
        events.publish(LoanEvent.of(type, loan.id(), loan.memberId(), loan.bookId(), date.today()));
    }
}
