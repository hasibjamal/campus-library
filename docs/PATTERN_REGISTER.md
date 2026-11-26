# Pattern Register

Every pattern below exists in the code, solves a problem we actually had, and has a test.
Counts: **Creational 2 · Structural 3 · Behavioral 6 = 11**. Rejected: **2**.

## Used patterns

| # | Pattern | Category | Lecture | Problem in the project | Key classes | Test | Alternative considered |
|---|---------|----------|---------|------------------------|-------------|------|------------------------|
| 1 | Builder | Creational | L9 | A room reservation has 7 fields and cross-field rules (opening hours, max 3 h, end > start). A constructor with 7 parameters was easy to call in the wrong order and validation was scattered. | `RoomReservation.Builder` | `RoomReservationBuilderTest` | Telescoping constructor + separate validator: rejected, order mistakes compile silently. |
| 2 | Abstract Factory | Creational | L3 | Five repositories must all come from the same storage family; mixing an in-memory loan repo with file-based members loses data silently. | `RepositoryFactory`, `InMemoryRepositoryFactory`, `FileRepositoryFactory` | `RepositoryContractTest` (run by `InMemoryRepositoryTest`, `FileRepositoryTest`) | `if (file) … else …` for every repository in App: rejected, five places to keep in sync. |
| 3 | Composite | Structural | L10 | The catalogue is a tree (sections inside sections). Counting, searching and reporting needed separate code for "book" and "section". | `CatalogComponent`, `Section`, `Book` | `CatalogCompositeTest` | Flat list of books with a `category` string: rejected, no nesting, no per-section counts. |
| 4 | Facade | Structural | L10 | The console UI would need five services, the command history and the date. | `LibraryFacade` | `AppSmokeTest`, `ConsoleMenuTest` | UI calls services directly: rejected, UI would know the wiring and the Command classes. |
| 5 | Proxy (protection) | Structural | L11 | Staff-only rooms and inactive members: access rules were mixing with overlap checking in one class. | `RoomBooking`, `AccessControlledRoomBooking`, `RoomBookingService` | `RoomBookingProxyTest` | `if` checks inside `RoomBookingService`: rejected, two responsibilities in one class (SRP). |
| 6 | State | Behavioral | L8 | Loan status was checked in many places ("can I return a reserved loan?"). | `LoanState`, `ReservedState`, `BorrowedState`, `OverdueState`, `ReturnedState`, `CancelledState`, `Loan` | `LoanStateTest` | Status enum + `switch`: rejected, transitions duplicated in every method. |
| 7 | Strategy | Behavioral | L8 | Fine rules differ by reader type and will keep changing. | `FineStrategy`, `DailyCappedFine`, `FinePolicy` | `FineStrategyTest` | `if (type == STUDENT) … else …`: rejected, each new rule edits old code. |
| 8 | Observer | Behavioral | L5 | Overdue and fine events must reach several receivers (notices, activity log) without `LoanService` knowing them. | `LoanEvents`, `LoanListener`, `OverdueNoticeListener`, `ActivityLog` | `ObserverTest`, `LoanServiceTest` | Direct calls from `LoanService` to e-mail code: rejected, service coupled to infra. |
| 9 | Command | Behavioral | L5 | Room bookings and cancellations must be undoable (requirement 5.2). | `Command`, `ReserveRoomCommand`, `CancelRoomCommand`, `CommandHistory` | `CommandUndoTest` | Memento of the whole reservation table: rejected (ADR-0005). |
| 10 | Chain of Responsibility | Behavioral | L12 | Four independent rules decide whether a loan is allowed; the list grows and the order matters. | `EligibilityCheck`, `ActiveMemberCheck`, `UnpaidFinesCheck`, `LoanLimitCheck`, `BookAvailableCheck` | `EligibilityChainTest` | One method with four `if`s: rejected, every new rule edits it. |
| 11 | Visitor | Behavioral | L13 | Several reports over the catalogue tree (tree print, author index); adding each to `Book`/`Section` bloats them. | `CatalogVisitor`, `CatalogueTreeReport`, `AuthorIndexReport` | `CatalogCompositeTest` | Methods on `CatalogComponent` per report: rejected, every report edits the tree classes. |

Architectural pattern (L14, not counted above): **Repository** — `Repository<T>`, `CatalogRepository`, `InMemoryRepository`, `FileRepository`; **Dependency Injection** via the composition root `App`.

## Combinations (L7)

| Combination | Where | Why together |
|-------------|-------|--------------|
| **State + Strategy** | `BorrowedState.returnItem` / `OverdueState.returnItem` call `FineStrategy.fineFor` | The state decides *whether* a fine applies; the strategy decides *how much*. Neither knows the other's rules. |
| **Composite + Visitor** | `Section.accept` / `Book.accept` with `CatalogVisitor` | Visitor walks the composite; new reports need no change to the tree. |
| **Command + Proxy** | `ReserveRoomCommand` calls `RoomBooking`, which is the proxy | Undoing a cancellation re-reserves through the proxy, so access rules apply to undo too. |

## Rejected patterns

| Pattern | Category | Where it was tempting | Why rejected | ADR |
|---------|----------|-----------------------|--------------|-----|
| Singleton | Creational | One `LoanEvents`, one `SimulatedDate`, one repository set | Hidden global state, tests interfere with each other. App creates exactly one instance and injects it. | [ADR-0008](adr/0008-reject-singleton.md) |
| Mediator | Behavioral | Coordinating loan, member and notification services | Only one direction of communication (loan → listeners); Observer is enough. A mediator would be a god class. | [ADR-0009](adr/0009-reject-mediator.md) |

## Removed in the pattern audit (week 7)

| What | Why removed | ADR |
|------|-------------|-----|
| `NoFine` strategy | Not used by any reader type → dead code. Will come back as a new class if an amnesty rule is requested. | [ADR-0010](adr/0010-pattern-audit.md) |
| Planned `DateProvider` interface | Would have had one implementation; concrete `SimulatedDate` used instead (YAGNI). | [ADR-0010](adr/0010-pattern-audit.md) |
