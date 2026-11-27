package edu.campus.library.domain.loan;

import edu.campus.library.domain.fine.FineStrategy;

import java.time.LocalDate;

/** The reader has the book and the due date has not been flagged as passed. */
public class BorrowedState implements LoanState {
    private static final long serialVersionUID = 1L;

    @Override
    public String name() {
        return "BORROWED";
    }

    @Override
    public void returnItem(Loan loan, LocalDate today, FineStrategy fines) {
        // State + Strategy combination: the state knows WHEN a fine applies,
        // the strategy knows HOW MUCH. A return before the scan still pays if late.
        loan.complete(today, fines.fineFor(loan.daysLate(today)));
        loan.changeState(new ReturnedState());
    }

    @Override
    public void renew(Loan loan, LocalDate today, int loanDays) {
        if (loan.daysLate(today) > 0) {
            throw new IllegalStateException("Loan " + loan.id() + " is already late and cannot be renewed");
        }
        if (loan.renewals() >= Loan.MAX_RENEWALS) {
            throw new IllegalStateException("Loan " + loan.id() + " was already renewed "
                    + Loan.MAX_RENEWALS + " time(s)");
        }
        loan.extend(loan.dueDate().plusDays(loanDays));
        // stays BORROWED: no state change
    }

    @Override
    public void markOverdue(Loan loan, LocalDate today) {
        if (loan.daysLate(today) <= 0) {
            throw new IllegalStateException("Loan " + loan.id() + " is not past its due date");
        }
        loan.changeState(new OverdueState());
    }

    @Override
    public boolean holdsBook() {
        return true;
    }
}
