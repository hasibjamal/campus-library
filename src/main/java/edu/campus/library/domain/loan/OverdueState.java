package edu.campus.library.domain.loan;

import edu.campus.library.domain.fine.FineStrategy;

import java.time.LocalDate;

/** Past the due date; an overdue notice has been sent. */
public class OverdueState implements LoanState {
    private static final long serialVersionUID = 1L;

    @Override
    public String name() {
        return "OVERDUE";
    }

    @Override
    public void returnItem(Loan loan, LocalDate today, FineStrategy fines) {
        loan.complete(today, fines.fineFor(loan.daysLate(today)));
        loan.changeState(new ReturnedState());
    }

    @Override
    public boolean holdsBook() {
        return true;
    }
}
