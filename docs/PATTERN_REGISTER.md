# Pattern Register

Every pattern below exists in the code, solves a problem we actually had, and has a test.
Counts: **Creational 2 · Structural 4 · Behavioral 5 = 11**.

## Used patterns

| # | Pattern | Category | Lecture | Problem in the project | Key classes | Test | Alternative considered |
|---|---------|----------|---------|------------------------|-------------|------|------------------------|
| 1 | Builder | Creational | L9 | A room reservation has 7 fields and cross-field rules (opening hours, max 3 h, end > start). A constructor with 7 parameters was easy to call in the wrong order and validation was scattered. | `RoomReservation.Builder` | `RoomReservationBuilderTest` | Telescoping constructor + separate validator: rejected, order mistakes compile silently. |
| 2 | Abstract Factory | Creational | L3 | Five repositories must all come from the same storage family; mixing an in-memory loan repo with file-based members loses data silently. | `RepositoryFactory`, `InMemoryRepositoryFactory`, `FileRepositoryFactory` | `RepositoryContractTest` (run by `InMemoryRepositoryTest`, `FileRepositoryTest`) | `if (file) … else …` for every repository in App: rejected, five places to keep in sync. |
| 3 | Composite | Structural | L10 | The catalogue is a tree (sections inside sections). Counting, searching and reporting needed separate code for "book" and "section". | `CatalogComponent`, `Section`, `Book` | `CatalogCompositeTest` | Flat list of books with a `category` string: rejected, no nesting, no per-section counts. |
| 4 | Facade | Structural | L10 | The console UI would need five services, the command history and the date. | `LibraryFacade` | `AppSmokeTest`, `ConsoleMenuTest` | UI calls services directly: rejected, UI would know the wiring and the Command classes. |
| 5 | Proxy (protection) | Structural | L11 | Staff-only rooms and inactive members: access rules were mixing with overlap checking in one class. | `RoomBooking`, `AccessControlledRoomBooking`, `RoomBookingService` | `RoomBookingProxyTest` | `if` checks inside `RoomBookingService`: rejected, two responsibilities in one class (SRP). |
| 6 | Adapter | Structural | L4 | The mail provider's SDK (`QuickMailClient.post(address, html, headers)` → status code) does not match our `NotificationChannel.send(member, subject, body)`. | `EmailChannel` (adapter), `QuickMailClient` (adaptee), `NotificationChannel` (target) | `EmailAdapterTest` | Call the SDK from `OverdueNoticeListener`: rejected, service tied to one vendor. |
| 7 | Template Method | Behavioral | L6 | Books arrive as CSV (old system, with header, quoted commas) and TSV (faculty sheet, no header). The read/skip/validate/add/report loop was the same; only parsing differed. | `CatalogImporter` (template `importFrom`), `CsvBookImporter`, `TsvBookImporter` | `CatalogImporterTest` | Strategy for the parser: also possible, but the fixed algorithm with hooks (`hasHeader`, `validate`) is the point here. |
| 8 | State | Behavioral | L8 | Loan status was checked in many places ("can I return a reserved loan?"). | `LoanState`, `ReservedState`, `BorrowedState`, `OverdueState`, `ReturnedState`, `CancelledState`, `Loan` | `LoanStateTest` | Status enum + `switch`: rejected, transitions duplicated in every method. |
| 9 | Strategy | Behavioral | L8 | Fine rules differ by reader type and will keep changing. | `FineStrategy`, `DailyCappedFine`, `FinePolicy` | `FineStrategyTest` | `if (type == STUDENT) … else …`: rejected, each new rule edits old code. |
| 10 | Observer | Behavioral | L5 | Overdue and fine events must reach several receivers (notices, activity log) without `LoanService` knowing them. | `LoanEvents`, `LoanListener`, `OverdueNoticeListener`, `ActivityLog` | `ObserverTest`, `LoanServiceTest` | Direct calls from `LoanService` to e-mail code: rejected, service coupled to infra. |
| 11 | Command | Behavioral | L5 | Room bookings and cancellations must be undoable (requirement 5.2). | `Command`, `ReserveRoomCommand`, `CancelRoomCommand`, `CommandHistory` | `CommandUndoTest` | Memento of the whole reservation table: rejected (ADR-0005). |

Architectural pattern (L14, not counted above): **Repository** — `Repository<T>`, `CatalogRepository`, `InMemoryRepository` (file implementation planned for M4); **Dependency Injection** via the composition root `App`.

## Combinations (L7)

| Combination | Where | Why together |
|-------------|-------|--------------|
| **State + Strategy** | `BorrowedState.returnItem` / `OverdueState.returnItem` call `FineStrategy.fineFor` | The state decides *whether* a fine applies; the strategy decides *how much*. Neither knows the other's rules. |
| **Command + Proxy** | `ReserveRoomCommand` calls `RoomBooking`, which is the proxy | Undoing a cancellation re-reserves through the proxy, so access rules apply to undo too. |

## Removed or simplified in the pattern audit (week 7)

| What | Why | ADR |
|------|-----|-----|
| `DateProvider` interface (M2) | Only one implementation, `SimulatedDate`; the interface was speculative generality. Removed, services take `SimulatedDate`. | [ADR-0010](adr/0010-pattern-audit.md) |
| Fine decorators (planned) | Three decorators for one formula; replaced by one configurable `DailyCappedFine` strategy in M3. | [ADR-0010](adr/0010-pattern-audit.md) |
