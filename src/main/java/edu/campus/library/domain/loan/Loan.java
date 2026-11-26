package edu.campus.library.domain.loan;

import edu.campus.library.domain.Identifiable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** A loan of one book to one member. Status rules are checked in each method. */
public class Loan implements Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String bookId;
    private final String memberId;
    private final LocalDate createdOn;
    private LocalDate borrowedOn;
    private LocalDate dueDate;
    private LocalDate returnedOn;
    private BigDecimal fine = BigDecimal.ZERO.setScale(2);
    private LoanStatus status = LoanStatus.RESERVED;

    public Loan(String id, String bookId, String memberId, LocalDate createdOn) {
        this.id = Objects.requireNonNull(id);
        this.bookId = Objects.requireNonNull(bookId);
        this.memberId = Objects.requireNonNull(memberId);
        this.createdOn = Objects.requireNonNull(createdOn);
    }

    public void pickUp(LocalDate today, int loanDays) {
        if (status != LoanStatus.RESERVED) {
            throw new IllegalStateException("Cannot pick up a loan that is " + status);
        }
        borrowedOn = today;
        dueDate = today.plusDays(loanDays);
        status = LoanStatus.BORROWED;
    }

    public void returnItem(LocalDate today, BigDecimal charged) {
        if (status != LoanStatus.BORROWED && status != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Cannot return a loan that is " + status);
        }
        returnedOn = today;
        fine = charged;
        status = LoanStatus.RETURNED;
    }

    public void markOverdue(LocalDate today) {
        if (status != LoanStatus.BORROWED) {
            throw new IllegalStateException("Cannot mark overdue a loan that is " + status);
        }
        if (daysLate(today) <= 0) {
            throw new IllegalStateException("Loan " + id + " is not past its due date");
        }
        status = LoanStatus.OVERDUE;
    }

    public void cancel() {
        if (status != LoanStatus.RESERVED) {
            throw new IllegalStateException("Cannot cancel a loan that is " + status);
        }
        status = LoanStatus.CANCELLED;
    }

    public long daysLate(LocalDate today) {
        if (dueDate == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, today));
    }

    public boolean holdsBook() {
        return status == LoanStatus.RESERVED || status == LoanStatus.BORROWED || status == LoanStatus.OVERDUE;
    }

    public LoanStatus status() {
        return status;
    }

    public String stateName() {
        return status.name();
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
        return id + " book=" + bookId + " member=" + memberId + " " + status
                + (dueDate != null ? " due=" + dueDate : "")
                + (fine.signum() > 0 ? " fine=" + fine : "");
    }
}
