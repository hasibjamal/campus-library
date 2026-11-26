package edu.campus.library.service.eligibility;

public class LoanLimitCheck extends EligibilityCheck {
    @Override
    protected void verify(LoanRequest r) {
        int max = r.member().type().maxLoans();
        if (r.activeLoans() >= max) {
            throw new LoanRefusedException("Loan limit of " + max + " reached");
        }
    }
}
