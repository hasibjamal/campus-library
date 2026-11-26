package edu.campus.library.service;

import java.util.concurrent.atomic.AtomicLong;

/** Readable sequential ids such as L-1001. Seeded above existing ids on start. */
public class IdGenerator {
    private final AtomicLong next;

    public IdGenerator(long start) {
        this.next = new AtomicLong(start);
    }

    public String next(String prefix) {
        return prefix + "-" + next.getAndIncrement();
    }
}
