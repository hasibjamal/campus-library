package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.event.LoanEvent;
import edu.campus.library.domain.event.LoanListener;
import edu.campus.library.domain.member.Member;

import java.util.List;

/** Observer: turns OVERDUE and FINE_CHARGED events into notices on every channel. */
public class OverdueNoticeListener implements LoanListener {
    private final Repository<Member> members;
    private final List<NotificationChannel> channels;

    public OverdueNoticeListener(Repository<Member> members, List<NotificationChannel> channels) {
        this.members = members;
        this.channels = List.copyOf(channels);
    }

    @Override
    public void onLoanEvent(LoanEvent e) {
        String subject;
        String body;
        switch (e.type()) {
            case OVERDUE -> {
                subject = "Overdue notice";
                body = "Book " + e.bookId() + " (loan " + e.loanId() + ") is overdue. Please return it.";
            }
            case FINE_CHARGED -> {
                subject = "Fine charged";
                body = "A fine of " + e.amount() + " was added for loan " + e.loanId() + ".";
            }
            default -> {
                return;
            }
        }
        members.findById(e.memberId()).ifPresent(m ->
                channels.forEach(c -> c.send(m, subject, body)));
    }
}
