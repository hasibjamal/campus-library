package edu.campus.library.service;

import edu.campus.library.domain.catalog.AuthorIndexReport;
import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.catalog.CatalogueTreeReport;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.service.command.CancelRoomCommand;
import edu.campus.library.service.command.CommandHistory;
import edu.campus.library.service.command.ReserveRoomCommand;
import edu.campus.library.service.importing.CatalogImporter;
import edu.campus.library.service.importing.ImportReport;

import java.io.Reader;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Facade (L10): one simple API for the UI over the catalogue, loan, member
 * and room subsystems. The UI depends on this class only.
 */
public class LibraryFacade {
    private final CatalogService catalog;
    private final LoanService loans;
    private final MemberService members;
    private final RoomBooking rooms;
    private final CommandHistory history;
    private final ActivityLog activity;
    private final SimulatedDate date;
    private final IdGenerator ids;
    private final Map<String, CatalogImporter> importers;

    public LibraryFacade(CatalogService catalog, LoanService loans, MemberService members,
                         RoomBooking rooms, CommandHistory history, ActivityLog activity,
                         SimulatedDate date, IdGenerator ids, List<CatalogImporter> importers) {
        this.catalog = catalog;
        this.loans = loans;
        this.members = members;
        this.rooms = rooms;
        this.history = history;
        this.activity = activity;
        this.date = date;
        this.ids = ids;
        this.importers = importers.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(CatalogImporter::format, i -> i));
    }

    // ----- catalogue -----
    public String catalogueTree() {
        return catalog.report(new CatalogueTreeReport());
    }

    public Map<String, TreeSet<String>> authorIndex() {
        return catalog.report(new AuthorIndexReport());
    }

    public List<Book> search(String text) {
        return catalog.search(text);
    }

    public Book addBook(String sectionPath, String title, String author, int year) {
        return catalog.addBook(sectionPath, title, author, year);
    }

    /** Imports books with the importer registered for the format (csv, tsv, ...). */
    public ImportReport importBooks(String format, Reader source) {
        CatalogImporter importer = importers.get(format.toLowerCase(java.util.Locale.ROOT));
        if (importer == null) {
            throw new NoSuchElementException("Unknown import format " + format + "; known: " + importers.keySet());
        }
        return importer.importFrom(source, catalog);
    }

    public boolean isAvailable(String bookId) {
        return loans.isAvailable(bookId);
    }

    // ----- members -----
    public List<Member> members() {
        return members.all();
    }

    public Member member(String id) {
        return members.get(id);
    }

    public BigDecimal payFine(String memberId, BigDecimal amount) {
        return members.payFine(memberId, amount);
    }

    // ----- loans -----
    public Loan borrow(String memberId, String bookId) {
        return loans.borrow(memberId, bookId);
    }

    public Loan reserveBook(String memberId, String bookId) {
        return loans.reserve(memberId, bookId);
    }

    public Loan pickUp(String loanId) {
        return loans.pickUp(loanId);
    }

    public Loan renew(String loanId) {
        return loans.renew(loanId);
    }

    public Loan returnBook(String loanId) {
        return loans.returnBook(loanId);
    }

    public List<Loan> scanOverdue() {
        return loans.scanOverdue();
    }

    public List<Loan> loans() {
        return loans.all();
    }

    public List<String> activity() {
        return activity.lines();
    }

    // ----- rooms (every change goes through a Command so it can be undone) -----
    public List<Room> rooms() {
        return rooms.rooms();
    }

    public RoomReservation reserveRoom(String memberId, String roomId, LocalDate day,
                                       int from, int to, String purpose) {
        RoomReservation r = RoomReservation.builder()
                .id(ids.next("R"))
                .room(roomId)
                .bookedBy(memberId)
                .on(day)
                .from(from)
                .to(to)
                .purpose(purpose)
                .build();
        history.run(new ReserveRoomCommand(rooms, r));
        return r;
    }

    public void cancelRoom(String reservationId) {
        history.run(new CancelRoomCommand(rooms, reservationId));
    }

    public Optional<String> undo() {
        return history.undo();
    }

    public List<RoomReservation> reservationsOn(LocalDate day) {
        return rooms.reservationsOn(day);
    }

    // ----- time (demo) -----
    public LocalDate today() {
        return date.today();
    }

    public void advanceDays(long days) {
        date.advanceDays(days);
    }
}
