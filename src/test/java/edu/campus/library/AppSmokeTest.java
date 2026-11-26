package edu.campus.library;

import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import edu.campus.library.service.LibraryFacade;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static edu.campus.library.TestFixtures.DAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Wires the real system through the composition root and runs the main use case via the Facade. */
class AppSmokeTest {
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

    @Test
    void mainUseCaseThroughFacade() {
        LibraryFacade lib = App.build(new InMemoryRepositoryFactory(), DAY, out);
        assertTrue(lib.catalogueTree().contains("[Computing] (3 books)"));
        assertEquals(3, lib.members().size());

        Loan loan = lib.borrow("M-1", "B-1");
        lib.advanceDays(20);
        assertEquals(1, lib.scanOverdue().size());
        lib.returnBook(loan.id());
        String printed = buf.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("[email to aigerim@campus.edu] Overdue notice"));
        assertTrue(printed.contains("[sms to Aigerim Sadykova] Fine charged"));
        assertEquals(new BigDecimal("3.00"), lib.member("M-1").fineBalance());
        assertEquals(new BigDecimal("3.00"), lib.payFine("M-1", new BigDecimal("3.00")));
        assertEquals(4, lib.activity().size());
    }

    @Test
    void roomBookingGoesThroughProxyAndCanBeUndone() {
        LibraryFacade lib = App.build(new InMemoryRepositoryFactory(), DAY, out);
        assertThrows(SecurityException.class, () -> lib.reserveRoom("M-1", "RM-3", DAY, 9, 10, "x"));
        RoomReservation r = lib.reserveRoom("M-3", "RM-3", DAY, 9, 10, "seminar");
        assertEquals(1, lib.reservationsOn(DAY).size());
        lib.cancelRoom(r.id());
        assertTrue(lib.reservationsOn(DAY).isEmpty());
        assertTrue(lib.undo().orElseThrow().startsWith("cancel"));
        assertEquals(r.toString(), lib.reservationsOn(DAY).get(0).toString());
        lib.undo();
        assertTrue(lib.reservationsOn(DAY).isEmpty());
        assertEquals(3, lib.rooms().size());
    }

    @Test
    void authorIndexAndSearch() {
        LibraryFacade lib = App.build(new InMemoryRepositoryFactory(), DAY, out);
        assertEquals(2, lib.search("design patterns").size());
        assertEquals(DAY, lib.today());
        Loan l = lib.reserveBook("M-1", "B-4");
        assertEquals("BORROWED", lib.pickUp(l.id()).stateName());
    }
}
