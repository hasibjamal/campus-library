package edu.campus.library.service;

import edu.campus.library.domain.event.LoanEvent;
import edu.campus.library.domain.event.LoanListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Observer: keeps a readable history of loan events for the librarian. */
public class ActivityLog implements LoanListener {
    private final List<String> lines = new ArrayList<>();

    @Override
    public void onLoanEvent(LoanEvent e) {
        lines.add(e.date() + " " + e.type() + " loan=" + e.loanId()
                + " member=" + e.memberId() + " book=" + e.bookId()
                + (e.amount().signum() > 0 ? " amount=" + e.amount() : ""));
    }

    public List<String> lines() {
        return Collections.unmodifiableList(lines);
    }
}
