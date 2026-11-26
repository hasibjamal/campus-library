package edu.campus.library.service;

import edu.campus.library.domain.event.LoanEvent;
import edu.campus.library.domain.event.LoanListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observer pattern (L5): the subject. LoanService publishes, listeners react. */
public class LoanEvents {
    private final List<LoanListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(LoanListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(LoanListener listener) {
        listeners.remove(listener);
    }

    public void publish(LoanEvent event) {
        listeners.forEach(l -> l.onLoanEvent(event));
    }
}
