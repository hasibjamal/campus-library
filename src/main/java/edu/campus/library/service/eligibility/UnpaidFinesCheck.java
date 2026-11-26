package edu.campus.library.service.eligibility;

import java.math.BigDecimal;

public class UnpaidFinesCheck extends EligibilityCheck {
    private final BigDecimal limit;

    public UnpaidFinesCheck(BigDecimal limit) {
        this.limit = limit;
    }

    @Override
    protected void verify(LoanRequest r) {
        if (r.member().fineBalance().compareTo(limit) > 0) {
            throw new LoanRefusedException("Unpaid fines " + r.member().fineBalance()
                    + " exceed the limit of " + limit);
        }
    }
}
