package edu.campus.library.service.eligibility;

public class BookAvailableCheck extends EligibilityCheck {
    @Override
    protected void verify(LoanRequest r) {
        if (!r.bookAvailable()) {
            throw new LoanRefusedException("Book " + r.book().id() + " is already on loan or reserved");
        }
    }
}
