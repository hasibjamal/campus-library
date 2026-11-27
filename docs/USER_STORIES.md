# User stories

| # | Story | Acceptance criteria | Covered by |
|---|-------|---------------------|-----------|
| US-1 | As a **reader** I want to browse the catalogue by section so that I can find books on a topic. | Tree shows every section with its book count; nested sections are indented. | `CatalogCompositeTest`, menu 1 |
| US-2 | As a **reader** I want to search by title or author so that I can find a book quickly. | Case-insensitive substring match on title or author; availability shown. | `CatalogAndMemberServiceTest`, menu 2 |
| US-3 | As a **librarian** I want to add a book under a section path so that the catalogue stays current. | `Science/Computing` creates missing sections and reuses existing ones; new id assigned. | `CatalogAndMemberServiceTest`, menu 4 |
| US-4 | As a **reader** I want to borrow a free book so that I can take it home. | Due date = today + 14 (student) / 28 (staff); book becomes unavailable. | `LoanServiceTest`, menu 6 |
| US-5 | As a **librarian** I want loans refused when rules are broken so that the collection is protected. | Refused if member inactive, unpaid fines > 5.00, loan limit reached (3 / 10), or book not free; the first broken rule is reported. | `EligibilityChainTest` |
| US-6 | As a **reader** I want to reserve a book and pick it up later so that nobody else takes it. | Reserved book is unavailable; pick-up starts the loan; reservation can be cancelled. | `LoanStateTest`, `LoanServiceTest`, menus 7–8 |
| US-7 | As a **librarian** I want late loans flagged and readers notified so that books come back. | Scan marks every borrowed loan past due as OVERDUE once; notice sent on e-mail and SMS. | `LoanServiceTest`, `ObserverTest`, menu 11 |
| US-8 | As a **librarian** I want fines charged on late return according to reader type so that rules are fair. | Student 0.50/day, cap 20.00; staff 0.25/day after 2 grace days, cap 10.00; fine added to balance; reader notified; can be paid. | `FineStrategyTest`, `LoanServiceTest`, menus 9, 12 |
| US-9 | As a **reader** I want to book a study room for up to 3 hours so that my group has a place to work. | 08:00–22:00 only; end after start; no overlap in the same room; staff-only rooms refused to students. | `RoomReservationBuilderTest`, `RoomBookingProxyTest`, menu 14 |
| US-10 | As a **reader** I want to undo my last room booking or cancellation so that mistakes are easy to fix. | Undo restores exactly the previous reservations; repeated undo walks back; "nothing to undo" when empty. | `CommandUndoTest`, menu 17 |
| US-11 | As a **reader** I want to renew a borrowed book once so that I can keep it longer. | Due date moves forward by the reader's loan period; refused when late, overdue, not yet collected, returned, or already renewed; logged as RENEWED. | `LoanStateTest`, `LoanServiceTest`, menu 21 |
