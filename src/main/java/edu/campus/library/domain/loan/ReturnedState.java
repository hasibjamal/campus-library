package edu.campus.library.domain.loan;

/** Final state: the book is back. */
public class ReturnedState implements LoanState {
    private static final long serialVersionUID = 1L;

    @Override
    public String name() {
        return "RETURNED";
    }
}
