package edu.campus.library.domain.member;

/** Reader category. Holds loan rules that differ per category (data, not behaviour). */
public enum MemberType {
    STUDENT(14, 3),
    STAFF(28, 10);

    private final int loanDays;
    private final int maxLoans;

    MemberType(int loanDays, int maxLoans) {
        this.loanDays = loanDays;
        this.maxLoans = maxLoans;
    }

    public int loanDays() {
        return loanDays;
    }

    public int maxLoans() {
        return maxLoans;
    }
}
