package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.catalog.Section;
import edu.campus.library.domain.fine.DailyCappedFine;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import edu.campus.library.infra.persistence.RepositoryFactory;
import edu.campus.library.service.eligibility.ActiveMemberCheck;
import edu.campus.library.service.eligibility.BookAvailableCheck;
import edu.campus.library.service.eligibility.EligibilityCheck;
import edu.campus.library.service.eligibility.LoanLimitCheck;
import edu.campus.library.service.eligibility.UnpaidFinesCheck;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static edu.campus.library.TestFixtures.DAY;
import static edu.campus.library.TestFixtures.staff;
import static edu.campus.library.TestFixtures.student;

/** Wires the loan services on top of any repository family, like App does. */
class LibraryWorld {
    final Repository<Member> memberRepo;
    final Repository<Loan> loanRepo;
    final CatalogRepository catalogRepo;
    final SimulatedDate date = new SimulatedDate(DAY);
    final LoanEvents events = new LoanEvents();
    final RecordingChannel channel = new RecordingChannel();
    final ActivityLog activity = new ActivityLog();
    final MemberService members;
    final CatalogService catalog;
    final LoanService loans;

    LibraryWorld(RepositoryFactory factory) {
        memberRepo = factory.members();
        loanRepo = factory.loans();
        catalogRepo = factory.catalog();
        IdGenerator ids = new IdGenerator(1000);
        if (memberRepo.findAll().isEmpty()) {
            memberRepo.save(student("M-1"));
            memberRepo.save(staff("M-2"));
            Section root = new Section("Catalogue");
            for (int i = 1; i <= 5; i++) {
                root.add(new Book("B-" + i, "Book " + i, "Author " + i, 2000 + i));
            }
            catalogRepo.saveRoot(root);
        }
        events.subscribe(new OverdueNoticeListener(memberRepo, List.of(channel)));
        events.subscribe(activity);
        EligibilityCheck chain = new ActiveMemberCheck();
        chain.then(new UnpaidFinesCheck(new BigDecimal("5.00")))
             .then(new LoanLimitCheck())
             .then(new BookAvailableCheck());
        FinePolicy fines = new FinePolicy(Map.of(
                MemberType.STUDENT, DailyCappedFine.student(),
                MemberType.STAFF, DailyCappedFine.staff()));
        members = new MemberService(memberRepo, ids);
        catalog = new CatalogService(catalogRepo, ids);
        loans = new LoanService(loanRepo, members, catalog, chain, fines, events, date, ids);
    }
}
