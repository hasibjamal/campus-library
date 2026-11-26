package edu.campus.library.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Something that happened to a loan. Published to all {@link LoanListener}s. */
public record LoanEvent(LoanEventType type, String loanId, String memberId, String bookId,
                        LocalDate date, BigDecimal amount) {

    public static LoanEvent of(LoanEventType type, String loanId, String memberId,
                               String bookId, LocalDate date) {
        return new LoanEvent(type, loanId, memberId, bookId, date, BigDecimal.ZERO.setScale(2));
    }
}
