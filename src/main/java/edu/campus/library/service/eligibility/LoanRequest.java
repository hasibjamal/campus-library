package edu.campus.library.service.eligibility;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.member.Member;

/** Everything the eligibility checks need, collected once by LoanService. */
public record LoanRequest(Member member, Book book, int activeLoans, boolean bookAvailable) {
}
