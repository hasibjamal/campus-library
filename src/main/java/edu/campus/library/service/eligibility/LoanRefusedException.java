package edu.campus.library.service.eligibility;

public class LoanRefusedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LoanRefusedException(String reason) {
        super(reason);
    }
}
