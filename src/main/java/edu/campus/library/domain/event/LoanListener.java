package edu.campus.library.domain.event;

/** Observer pattern (L5): subscriber side. */
@FunctionalInterface
public interface LoanListener {
    void onLoanEvent(LoanEvent event);
}
