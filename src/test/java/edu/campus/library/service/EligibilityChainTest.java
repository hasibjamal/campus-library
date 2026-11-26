package edu.campus.library.service;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.member.Member;
import edu.campus.library.service.eligibility.ActiveMemberCheck;
import edu.campus.library.service.eligibility.BookAvailableCheck;
import edu.campus.library.service.eligibility.EligibilityCheck;
import edu.campus.library.service.eligibility.LoanLimitCheck;
import edu.campus.library.service.eligibility.LoanRefusedException;
import edu.campus.library.service.eligibility.LoanRequest;
import edu.campus.library.service.eligibility.UnpaidFinesCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Chain of Responsibility: each link refuses for its own reason; order matters. */
class EligibilityChainTest {
    private final Book book = new Book("B-1", "T", "A", 2000);
    private EligibilityCheck chain;
    private Member member;

    @BeforeEach
    void setUp() {
        chain = new ActiveMemberCheck();
        chain.then(new UnpaidFinesCheck(new BigDecimal("5.00")))
             .then(new LoanLimitCheck())
             .then(new BookAvailableCheck());
        member = student("M-1");
    }

    private String refusal(LoanRequest r) {
        return assertThrows(LoanRefusedException.class, () -> chain.check(r)).getMessage();
    }

    @Test
    void passesWhenAllRulesHold() {
        assertDoesNotThrow(() -> chain.check(new LoanRequest(member, book, 0, true)));
    }

    @Test
    void inactiveMemberIsRefused() {
        member.deactivate();
        assertTrue(refusal(new LoanRequest(member, book, 0, true)).contains("not active"));
    }

    @Test
    void unpaidFinesAboveLimitAreRefused() {
        member.addFine(new BigDecimal("5.01"));
        assertTrue(refusal(new LoanRequest(member, book, 0, true)).contains("Unpaid fines"));
    }

    @Test
    void finesAtLimitAreAllowed() {
        member.addFine(new BigDecimal("5.00"));
        assertDoesNotThrow(() -> chain.check(new LoanRequest(member, book, 0, true)));
    }

    @Test
    void loanLimitIsRefused() {
        assertTrue(refusal(new LoanRequest(member, book, 3, true)).contains("limit"));
    }

    @Test
    void unavailableBookIsRefused() {
        assertTrue(refusal(new LoanRequest(member, book, 0, false)).contains("already on loan"));
    }

    @Test
    void firstFailingLinkWins() {
        member.deactivate();
        member.addFine(new BigDecimal("50"));
        assertTrue(refusal(new LoanRequest(member, book, 9, false)).contains("not active"));
    }
}
