package edu.campus.library.service;

import edu.campus.library.domain.event.LoanEvent;
import edu.campus.library.domain.event.LoanEventType;
import edu.campus.library.domain.event.LoanListener;
import edu.campus.library.domain.member.Member;
import edu.campus.library.infra.persistence.InMemoryRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static edu.campus.library.TestFixtures.DAY;
import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Observer: subject delivers to every subscriber; notice listener fans out to all channels. */
class ObserverTest {

    @Test
    void publishReachesAllSubscribersUntilUnsubscribed() {
        LoanEvents events = new LoanEvents();
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        LoanListener la = e -> a.add(e.loanId());
        events.subscribe(la);
        events.subscribe(e -> b.add(e.loanId()));
        events.publish(LoanEvent.of(LoanEventType.BORROWED, "L-1", "M-1", "B-1", DAY));
        events.unsubscribe(la);
        events.publish(LoanEvent.of(LoanEventType.RETURNED, "L-2", "M-1", "B-1", DAY));
        assertEquals(List.of("L-1"), a);
        assertEquals(List.of("L-1", "L-2"), b);
    }

    @Test
    void noticeGoesToEveryChannelOnlyForOverdueAndFines() {
        InMemoryRepository<Member> members = new InMemoryRepository<>();
        members.save(student("M-1"));
        RecordingChannel c1 = new RecordingChannel();
        RecordingChannel c2 = new RecordingChannel();
        OverdueNoticeListener listener = new OverdueNoticeListener(members, List.of(c1, c2));

        listener.onLoanEvent(LoanEvent.of(LoanEventType.BORROWED, "L-1", "M-1", "B-1", DAY));
        assertTrue(c1.sent.isEmpty());

        listener.onLoanEvent(LoanEvent.of(LoanEventType.OVERDUE, "L-1", "M-1", "B-1", DAY));
        listener.onLoanEvent(new LoanEvent(LoanEventType.FINE_CHARGED, "L-1", "M-1", "B-1", DAY,
                new BigDecimal("1.50")));
        assertEquals(2, c1.sent.size());
        assertEquals(c1.sent, c2.sent);
        assertTrue(c1.sent.get(1).contains("1.50"));
    }

    @Test
    void unknownMemberIsIgnored() {
        RecordingChannel c = new RecordingChannel();
        new OverdueNoticeListener(new InMemoryRepository<>(), List.of(c))
                .onLoanEvent(LoanEvent.of(LoanEventType.OVERDUE, "L-1", "M-9", "B-1", DAY));
        assertTrue(c.sent.isEmpty());
    }
}
