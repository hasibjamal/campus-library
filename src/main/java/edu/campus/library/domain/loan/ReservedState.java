package edu.campus.library.domain.loan;

import java.time.LocalDate;

/** The book is held for the reader but not yet collected. */
public class ReservedState implements LoanState {
    private static final long serialVersionUID = 1L;

    @Override
    public String name() {
        return "RESERVED";
    }

    @Override
    public void pickUp(Loan loan, LocalDate today, int loanDays) {
        loan.start(today, today.plusDays(loanDays));
        loan.changeState(new BorrowedState());
    }

    @Override
    public void cancel(Loan loan) {
        loan.changeState(new CancelledState());
    }

    @Override
    public boolean holdsBook() {
        return true;
    }
}
