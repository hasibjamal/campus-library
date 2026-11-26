package edu.campus.library.service.eligibility;

public class ActiveMemberCheck extends EligibilityCheck {
    @Override
    protected void verify(LoanRequest r) {
        if (!r.member().isActive()) {
            throw new LoanRefusedException("Membership of " + r.member().id() + " is not active");
        }
    }
}
