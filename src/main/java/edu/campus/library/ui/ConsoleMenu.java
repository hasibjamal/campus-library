package edu.campus.library.ui;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.service.ActivityLog;
import edu.campus.library.service.CatalogService;
import edu.campus.library.service.IdGenerator;
import edu.campus.library.service.LoanService;
import edu.campus.library.service.MemberService;
import edu.campus.library.service.RoomBookingService;
import edu.campus.library.service.SimulatedDate;
import edu.campus.library.service.command.CancelRoomCommand;
import edu.campus.library.service.command.CommandHistory;
import edu.campus.library.service.command.ReserveRoomCommand;
import edu.campus.library.service.importing.CatalogImporter;
import edu.campus.library.service.importing.ImportReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/** Console UI. Calls the services directly. */
public class ConsoleMenu {
    private final CatalogService catalog;
    private final LoanService loans;
    private final MemberService members;
    private final RoomBookingService rooms;
    private final CommandHistory history;
    private final ActivityLog activity;
    private final SimulatedDate date;
    private final IdGenerator ids;
    private final Map<String, CatalogImporter> importers;
    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleMenu(CatalogService catalog, LoanService loans, MemberService members,
                       RoomBookingService rooms, CommandHistory history, ActivityLog activity,
                       SimulatedDate date, IdGenerator ids, Map<String, CatalogImporter> importers,
                       InputStream in, PrintStream out) {
        this.catalog = catalog;
        this.loans = loans;
        this.members = members;
        this.rooms = rooms;
        this.history = history;
        this.activity = activity;
        this.date = date;
        this.ids = ids;
        this.importers = importers;
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
        out.println("Today: " + date.today());
        out.println(" Catalogue:  1 tree   2 search   4 add book");
        out.println(" Loans:      5 members  6 borrow  7 reserve book  8 pick up  9 return");
        out.println("            10 list loans  11 overdue scan  12 pay fine");
        out.println(" Rooms:     13 rooms  14 reserve room  15 cancel  16 day schedule  17 undo");
        out.println(" Other:     18 advance date  19 activity log  20 import books   0 exit");
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> out.print(catalog.treeText());
            case "2" -> printBooks(catalog.search(ask("Title or author contains")));
            case "4" -> out.println("Added " + catalog.addBook(ask("Section path (e.g. Science/Computing)"),
                    ask("Title"), ask("Author"), Integer.parseInt(ask("Year"))));
            case "5" -> members.all().forEach(out::println);
            case "6" -> out.println("Borrowed: " + loans.borrow(ask("Member id"), ask("Book id")));
            case "7" -> out.println("Reserved: " + loans.reserve(ask("Member id"), ask("Book id")));
            case "8" -> out.println("Picked up: " + loans.pickUp(ask("Loan id")));
            case "9" -> out.println("Returned: " + loans.returnBook(ask("Loan id")));
            case "10" -> printLoans(loans.all());
            case "11" -> out.println("Flagged overdue: " + loans.scanOverdue().size());
            case "12" -> out.println("Paid " + members.payFine(ask("Member id"), new BigDecimal(ask("Amount"))));
            case "13" -> rooms.rooms().forEach(out::println);
            case "14" -> out.println("Booked: " + reserveRoom());
            case "15" -> {
                history.run(new CancelRoomCommand(rooms, ask("Reservation id")));
                out.println("Cancelled.");
            }
            case "16" -> rooms.reservationsOn(askDate()).forEach(out::println);
            case "17" -> out.println(history.undo().map(d -> "Undone: " + d).orElse("Nothing to undo."));
            case "18" -> date.advanceDays(Long.parseLong(ask("Days")));
            case "19" -> activity.lines().forEach(out::println);
            case "20" -> importBooks();
            default -> out.println("Unknown option");
        }
    }

    private RoomReservation reserveRoom() {
        String memberId = ask("Member id");
        String roomId = ask("Room id");
        RoomReservation r = new RoomReservation(ids.next("R"), roomId, memberId, askDate(),
                Integer.parseInt(ask("From hour")), Integer.parseInt(ask("To hour")), ask("Purpose"));
        history.run(new ReserveRoomCommand(rooms, r));
        return r;
    }

    private void importBooks() {
        String format = ask("Format (csv/tsv)");
        CatalogImporter importer = importers.get(format.toLowerCase(Locale.ROOT));
        if (importer == null) {
            throw new NoSuchElementException("Unknown import format " + format + "; known: " + importers.keySet());
        }
        Path file = Path.of(ask("File path"));
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            ImportReport report = importer.importFrom(r, catalog);
            out.println("Imported " + report.imported() + " books");
            report.errors().forEach(e -> out.println("  skipped " + e));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    private LocalDate askDate() {
        String s = ask("Date (YYYY-MM-DD, empty = today)");
        return s == null || s.isBlank() ? date.today() : LocalDate.parse(s.trim());
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            out.println("No books found.");
        }
        books.forEach(b -> out.println(b + (loans.isAvailable(b.id()) ? "  [available]" : "  [on loan]")));
    }

    private void printLoans(List<Loan> list) {
        if (list.isEmpty()) {
            out.println("No loans.");
        }
        list.forEach(out::println);
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
