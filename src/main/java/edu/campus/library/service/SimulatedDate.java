package edu.campus.library.service;

import java.time.LocalDate;

/**
 * The current date used by the services. Injected (never static) so tests
 * and the demo can move time forward to make loans overdue.
 */
public class SimulatedDate implements DateProvider {
    private LocalDate today;

    public SimulatedDate(LocalDate start) {
        this.today = start;
    }

    @Override
    public LocalDate today() {
        return today;
    }

    public void advanceDays(long days) {
        if (days < 0) {
            throw new IllegalArgumentException("Time only moves forward");
        }
        today = today.plusDays(days);
    }
}
