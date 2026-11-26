package edu.campus.library;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.fine.DailyCappedFine;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.infra.notification.EmailChannel;
import edu.campus.library.infra.notification.SmsChannel;
import edu.campus.library.infra.notification.vendor.QuickMailClient;
import edu.campus.library.infra.persistence.DemoData;
import edu.campus.library.infra.persistence.FileRepositoryFactory;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import edu.campus.library.infra.persistence.RepositoryFactory;
import edu.campus.library.service.AccessControlledRoomBooking;
import edu.campus.library.service.ActivityLog;
import edu.campus.library.service.CatalogService;
import edu.campus.library.service.FinePolicy;
import edu.campus.library.service.IdGenerator;
import edu.campus.library.service.LibraryFacade;
import edu.campus.library.service.LoanEvents;
import edu.campus.library.service.LoanService;
import edu.campus.library.service.MemberService;
import edu.campus.library.service.NotificationChannel;
import edu.campus.library.service.OverdueNoticeListener;
import edu.campus.library.service.RoomBooking;
import edu.campus.library.service.RoomBookingService;
import edu.campus.library.service.SimulatedDate;
import edu.campus.library.service.command.CommandHistory;
import edu.campus.library.service.eligibility.ActiveMemberCheck;
import edu.campus.library.service.eligibility.BookAvailableCheck;
import edu.campus.library.service.eligibility.EligibilityCheck;
import edu.campus.library.service.eligibility.LoanLimitCheck;
import edu.campus.library.service.eligibility.UnpaidFinesCheck;
import edu.campus.library.service.importing.CsvBookImporter;
import edu.campus.library.service.importing.TsvBookImporter;
import edu.campus.library.ui.ConsoleMenu;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Composition root (L14). The only place that knows concrete classes and
 * wires them together. No Singletons, no service locator.
 *
 * Run: mvn -q compile exec:java                      (in-memory store)
 *      mvn -q compile exec:java -Dexec.args=--store=file
 */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        boolean fileStore = List.of(args).contains("--store=file");
        RepositoryFactory factory = fileStore
                ? new FileRepositoryFactory(Path.of("data"))
                : new InMemoryRepositoryFactory();
        System.out.println("Store: " + (fileStore ? "file (./data)" : "in memory"));
        LibraryFacade library = build(factory, LocalDate.now(), System.out);
        new ConsoleMenu(library, System.in, System.out).run();
    }

    /** Builds the whole object graph. Public so a smoke test can wire the real system. */
    public static LibraryFacade build(RepositoryFactory factory, LocalDate startDate, PrintStream out) {
        // persistence (Abstract Factory chooses the family)
        Repository<Member> members = factory.members();
        Repository<Loan> loans = factory.loans();
        Repository<Room> rooms = factory.rooms();
        Repository<RoomReservation> reservations = factory.roomReservations();
        CatalogRepository catalog = factory.catalog();
        DemoData.seedIfEmpty(members, rooms, catalog);

        SimulatedDate date = new SimulatedDate(startDate);
        IdGenerator ids = new IdGenerator(nextFreeId(members, loans, reservations, catalog));

        // Observer: subject + subscribers
        LoanEvents events = new LoanEvents();
        List<NotificationChannel> channels = List.of(
                new EmailChannel(new QuickMailClient(out)),   // Adapter over the vendor SDK
                new SmsChannel(out));
        events.subscribe(new OverdueNoticeListener(members, channels));
        ActivityLog activity = new ActivityLog();
        events.subscribe(activity);

        // Strategy: one fine rule per reader type
        FinePolicy fines = new FinePolicy(Map.of(
                MemberType.STUDENT, DailyCappedFine.student(),
                MemberType.STAFF, DailyCappedFine.staff()));

        // Chain of Responsibility: order of eligibility checks
        EligibilityCheck eligibility = new ActiveMemberCheck();
        eligibility.then(new UnpaidFinesCheck(new BigDecimal("5.00")))
                   .then(new LoanLimitCheck())
                   .then(new BookAvailableCheck());

        MemberService memberService = new MemberService(members, ids);
        CatalogService catalogService = new CatalogService(catalog, ids);
        LoanService loanService = new LoanService(loans, memberService, catalogService,
                eligibility, fines, events, date, ids);

        // Proxy wraps the real room service
        RoomBooking roomBooking = new AccessControlledRoomBooking(
                new RoomBookingService(rooms, reservations), rooms, members);

        // Facade for the UI
        return new LibraryFacade(catalogService, loanService, memberService, roomBooking,
                new CommandHistory(), activity, date, ids,
                List.of(new CsvBookImporter(), new TsvBookImporter()));   // Template Method variants
    }

    /** Ids continue after the highest number already stored, so file data never clashes. */
    static long nextFreeId(Repository<Member> m, Repository<Loan> l,
                           Repository<RoomReservation> r, CatalogRepository c) {
        Stream<String> all = Stream.of(
                m.findAll().stream().map(Member::id),
                l.findAll().stream().map(Loan::id),
                r.findAll().stream().map(RoomReservation::id),
                c.loadRoot().books().map(b -> b.id())).flatMap(s -> s);
        long max = all.mapToLong(App::numberPart).max().orElse(0);
        return Math.max(1000, max + 1);
    }

    private static long numberPart(String id) {
        int dash = id.lastIndexOf('-');
        try {
            return Long.parseLong(id.substring(dash + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
