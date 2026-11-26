package edu.campus.library;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;
import edu.campus.library.infra.notification.EmailChannel;
import edu.campus.library.infra.notification.vendor.QuickMailClient;
import edu.campus.library.infra.persistence.DemoData;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import edu.campus.library.infra.persistence.RepositoryFactory;
import edu.campus.library.service.ActivityLog;
import edu.campus.library.service.CatalogService;
import edu.campus.library.service.IdGenerator;
import edu.campus.library.service.LoanEvents;
import edu.campus.library.service.LoanService;
import edu.campus.library.service.MemberService;
import edu.campus.library.service.NotificationChannel;
import edu.campus.library.service.OverdueNoticeListener;
import edu.campus.library.service.RoomBookingService;
import edu.campus.library.service.SimulatedDate;
import edu.campus.library.service.command.CommandHistory;
import edu.campus.library.service.importing.CatalogImporter;
import edu.campus.library.service.importing.CsvBookImporter;
import edu.campus.library.service.importing.TsvBookImporter;
import edu.campus.library.ui.ConsoleMenu;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Composition root (L14). The only place that knows concrete classes and
 * wires them together. No Singletons, no service locator.
 *
 * Run: mvn -q compile exec:java
 */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        System.out.println("Store: in memory");
        build(new InMemoryRepositoryFactory(), LocalDate.now(), System.in, System.out).run();
    }

    /** Builds the whole object graph. Public so a smoke test can wire the real system. */
    public static ConsoleMenu build(RepositoryFactory factory, LocalDate startDate, InputStream in, PrintStream out) {
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
        List<NotificationChannel> channels = List.of(new EmailChannel(new QuickMailClient(out)));
        events.subscribe(new OverdueNoticeListener(members, channels));
        ActivityLog activity = new ActivityLog();
        events.subscribe(activity);

        MemberService memberService = new MemberService(members, ids);
        CatalogService catalogService = new CatalogService(catalog, ids);
        LoanService loanService = new LoanService(loans, memberService, catalogService, events, date, ids);
        RoomBookingService roomService = new RoomBookingService(rooms, reservations, members);

        Map<String, CatalogImporter> importers = Stream.of(new CsvBookImporter(), new TsvBookImporter())
                .collect(Collectors.toMap(CatalogImporter::format, i -> i));

        return new ConsoleMenu(catalogService, loanService, memberService, roomService,
                new CommandHistory(), activity, date, ids, importers, in, out);
    }

    /** Ids continue after the highest number already stored. */
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
