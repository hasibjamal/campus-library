package edu.campus.library.domain.loan;

import edu.campus.library.domain.Identifiable;
import edu.campus.library.domain.fine.FineStrategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Context of the State pattern: delegates every status-dependent action to its state. */
public class Loan implements Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_RENEWALS = 1;

    private final String id;
    private final String bookId;
    private final String memberId;
    private final LocalDate createdOn;
    private LocalDate borrowedOn;
    private LocalDate dueDate;
    private LocalDate returnedOn;
    private BigDecimal fine = BigDecimal.ZERO.setScale(2);
    private LoanState state = new ReservedState();
    private int renewals;

    public Loan(String id, String bookId, String memberId, LocalDate createdOn) {
        this.id = Objects.requireNonNull(id);
        this.bookId = Objects.requireNonNull(bookId);
        this.memberId = Objects.requireNonNull(memberId);
        this.createdOn = Objects.requireNonNull(createdOn);
    }

    // ----- actions, delegated to the current state -----

    public void pickUp(LocalDate today, int loanDays) {
        state.pickUp(this, today, loanDays);
    }

    public void returnItem(LocalDate today, FineStrategy fines) {
        state.returnItem(this, today, fines);
    }

    public void renew(LocalDate today, int loanDays) {
        state.renew(this, today, loanDays);
    }

    public void markOverdue(LocalDate today) {
        state.markOverdue(this, today);
    }

    public void cancel() {
        state.cancel(this);
    }

    // ----- used only by state classes (package-private) -----

    void changeState(LoanState next) {
        this.state = Objects.requireNonNull(next);
    }

    void start(LocalDate borrowed, LocalDate due) {
        this.borrowedOn = borrowed;
        this.dueDate = due;
    }

    void extend(LocalDate newDue) {
        this.dueDate = newDue;
        this.renewals++;
    }

    void complete(LocalDate returned, BigDecimal charged) {
        this.returnedOn = returned;
        this.fine = charged;
    }

    // ----- queries -----

    public long daysLate(LocalDate today) {
        if (dueDate == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, today));
    }

    public int renewals() {
        return renewals;
    }

    public boolean holdsBook() {
        return state.holdsBook();
    }

    public String stateName() {
        return state.name();
    }

    @Override
    public String id() {
        return id;
    }

    public String bookId() {
        return bookId;
    }

    public String memberId() {
        return memberId;
    }

    public LocalDate createdOn() {
        return createdOn;
    }

    public LocalDate borrowedOn() {
        return borrowedOn;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public LocalDate returnedOn() {
        return returnedOn;
    }

    public BigDecimal fine() {
        return fine;
    }

    @Override
    public String toString() {
        return id + " book=" + bookId + " member=" + memberId + " " + state.name()
                + (dueDate != null ? " due=" + dueDate : "")
                + (fine.signum() > 0 ? " fine=" + fine : "");
    }
}
