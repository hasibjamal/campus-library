package edu.campus.library.domain.loan;

/** Final state: a reservation that was never collected. */
public class CancelledState implements LoanState {
    private static final long serialVersionUID = 1L;

    @Override
    public String name() {
        return "CANCELLED";
    }
}
