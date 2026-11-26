package edu.campus.library.service.eligibility;

/**
 * Chain of Responsibility (L12). Each check either refuses the loan or
 * passes the request to the next check. The order is set in App.
 */
public abstract class EligibilityCheck {
    private EligibilityCheck next;

    /** Links the next check and returns it, so chains read left to right. */
    public EligibilityCheck then(EligibilityCheck nextCheck) {
        this.next = nextCheck;
        return nextCheck;
    }

    public final void check(LoanRequest request) {
        verify(request);
        if (next != null) {
            next.check(request);
        }
    }

    /** Throws {@link LoanRefusedException} if this rule is broken. */
    protected abstract void verify(LoanRequest request);
}
