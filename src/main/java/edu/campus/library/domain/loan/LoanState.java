package edu.campus.library.domain.loan;

import edu.campus.library.domain.fine.FineStrategy;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * State pattern (L8). Each loan status is a class that decides which
 * transitions are legal. Illegal transitions throw instead of being
 * checked with if/else in many places.
 */
public interface LoanState extends Serializable {
    String name();

    default void pickUp(Loan loan, LocalDate today, int loanDays) {
        throw illegal("pick up");
    }

    default void returnItem(Loan loan, LocalDate today, FineStrategy fines) {
        throw illegal("return");
    }

    default void renew(Loan loan, LocalDate today, int loanDays) {
        throw illegal("renew");
    }

    default void markOverdue(Loan loan, LocalDate today) {
        throw illegal("mark overdue");
    }

    default void cancel(Loan loan) {
        throw illegal("cancel");
    }

    /** True while the loan holds the book (reserved, borrowed or overdue). */
    default boolean holdsBook() {
        return false;
    }

    private IllegalStateException illegal(String action) {
        return new IllegalStateException("Cannot " + action + " a loan that is " + name());
    }
}
