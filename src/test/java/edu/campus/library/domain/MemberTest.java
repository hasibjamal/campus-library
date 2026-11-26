package edu.campus.library.domain;

import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {

    @Test
    void finesAccumulateAndPaymentsNeverGoBelowZero() {
        Member m = student("M-1");
        m.addFine(new BigDecimal("2.50"));
        m.addFine(new BigDecimal("1.00"));
        assertEquals(new BigDecimal("3.50"), m.fineBalance());
        assertEquals(new BigDecimal("3.50"), m.payFine(new BigDecimal("10")));
        assertEquals(new BigDecimal("0.00"), m.fineBalance());
    }

    @Test
    void rejectsInvalidAmounts() {
        Member m = student("M-1");
        assertThrows(IllegalArgumentException.class, () -> m.addFine(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> m.payFine(BigDecimal.ZERO));
    }

    @Test
    void deactivation() {
        Member m = student("M-1");
        m.deactivate();
        assertFalse(m.isActive());
    }

    @Test
    void typeRules() {
        assertEquals(14, MemberType.STUDENT.loanDays());
        assertEquals(3, MemberType.STUDENT.maxLoans());
        assertEquals(28, MemberType.STAFF.loanDays());
    }
}
