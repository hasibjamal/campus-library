package edu.campus.library.ui;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.service.LibraryFacade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Console UI. Depends only on {@link LibraryFacade}; it never sees
 * repositories, factories or infra classes.
 */
public class ConsoleMenu {
    private final LibraryFacade library;
    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleMenu(LibraryFacade library, InputStream in, PrintStream out) {
        this.library = library;
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
    }

    public void run() {
        out.println("=== Campus Library and Room Booking ===");
        while (true) {
            printMenu();
            String choice = ask("Choose");
            if (choice == null || choice.equals("0")) {
                out.println("Bye.");
                return;
            }
            try {
                handle(choice);
            } catch (RuntimeException e) {
                out.println("! " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("Today: " + library.today());
        out.println(" Catalogue:  1 tree   2 search   3 author index   4 add book");
        out.println(" Loans:      5 members  6 borrow  7 reserve book  8 pick up  9 return");
        out.println("            10 list loans  11 overdue scan  12 pay fine");
        out.println(" Rooms:     13 rooms  14 reserve room  15 cancel  16 day schedule  17 undo");
        out.println(" Other:     18 advance date  19 activity log  20 import books   0 exit");
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> out.print(library.catalogueTree());
            case "2" -> printBooks(library.search(ask("Title or author contains")));
            case "3" -> library.authorIndex().forEach((a, titles) -> out.println(a + ": " + titles));
            case "4" -> out.println("Added " + library.addBook(ask("Section path (e.g. Science/Computing)"),
                    ask("Title"), ask("Author"), Integer.parseInt(ask("Year"))));
            case "5" -> library.members().forEach(out::println);
            case "6" -> out.println("Borrowed: " + library.borrow(ask("Member id"), ask("Book id")));
            case "7" -> out.println("Reserved: " + library.reserveBook(ask("Member id"), ask("Book id")));
            case "8" -> out.println("Picked up: " + library.pickUp(ask("Loan id")));
            case "9" -> out.println("Returned: " + library.returnBook(ask("Loan id")));
            case "10" -> printLoans(library.loans());
            case "11" -> out.println("Flagged overdue: " + library.scanOverdue().size());
            case "12" -> out.println("Paid " + library.payFine(ask("Member id"), new BigDecimal(ask("Amount"))));
            case "13" -> library.rooms().forEach(out::println);
            case "14" -> out.println("Booked: " + reserveRoom());
            case "15" -> {
                library.cancelRoom(ask("Reservation id"));
                out.println("Cancelled.");
            }
            case "16" -> library.reservationsOn(askDate()).forEach(out::println);
            case "17" -> out.println(library.undo().map(d -> "Undone: " + d).orElse("Nothing to undo."));
            case "18" -> library.advanceDays(Long.parseLong(ask("Days")));
            case "19" -> library.activity().forEach(out::println);
            case "20" -> importBooks();
            default -> out.println("Unknown option");
        }
    }

    private void importBooks() {
        String format = ask("Format (csv/tsv)");
        Path file = Path.of(ask("File path"));
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            var report = library.importBooks(format, r);
            out.println("Imported " + report.imported() + " books");
            report.errors().forEach(e -> out.println("  skipped " + e));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    private RoomReservation reserveRoom() {
        return library.reserveRoom(ask("Member id"), ask("Room id"), askDate(),
                Integer.parseInt(ask("From hour")), Integer.parseInt(ask("To hour")), ask("Purpose"));
    }

    private LocalDate askDate() {
        String s = ask("Date (YYYY-MM-DD, empty = today)");
        return s == null || s.isBlank() ? library.today() : LocalDate.parse(s.trim());
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            out.println("No books found.");
        }
        books.forEach(b -> out.println(b + (library.isAvailable(b.id()) ? "  [available]" : "  [on loan]")));
    }

    private void printLoans(List<Loan> loans) {
        if (loans.isEmpty()) {
            out.println("No loans.");
        }
        loans.forEach(out::println);
    }

    private String ask(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        try {
            String line = in.readLine();
            return line == null ? null : line.trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
