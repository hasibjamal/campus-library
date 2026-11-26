# Pattern Register

Every pattern below exists in the code, solves a problem we actually had, and has a test.
Counts: **Creational 1 · Structural 2 · Behavioral 3 = 6**.

## Used patterns

| # | Pattern | Category | Lecture | Problem in the project | Key classes | Test | Alternative considered |
|---|---------|----------|---------|------------------------|-------------|------|------------------------|
| 1 | Abstract Factory | Creational | L3 | Five repositories must all come from the same storage family; mixing an in-memory loan repo with file-based members loses data silently. | `RepositoryFactory`, `InMemoryRepositoryFactory`, `FileRepositoryFactory` | `RepositoryContractTest` (run by `InMemoryRepositoryTest`, `FileRepositoryTest`) | `if (file) … else …` for every repository in App: rejected, five places to keep in sync. |
| 2 | Composite | Structural | L10 | The catalogue is a tree (sections inside sections). Counting, searching and reporting needed separate code for "book" and "section". | `CatalogComponent`, `Section`, `Book` | `CatalogCompositeTest` | Flat list of books with a `category` string: rejected, no nesting, no per-section counts. |
| 3 | Adapter | Structural | L4 | The mail provider's SDK (`QuickMailClient.post(address, html, headers)` → status code) does not match our `NotificationChannel.send(member, subject, body)`. | `EmailChannel` (adapter), `QuickMailClient` (adaptee), `NotificationChannel` (target) | `EmailAdapterTest` | Call the SDK from `OverdueNoticeListener`: rejected, service tied to one vendor. |
| 4 | Template Method | Behavioral | L6 | Books arrive as CSV (old system, with header, quoted commas) and TSV (faculty sheet, no header). The read/skip/validate/add/report loop was the same; only parsing differed. | `CatalogImporter` (template `importFrom`), `CsvBookImporter`, `TsvBookImporter` | `CatalogImporterTest` | Strategy for the parser: also possible, but the fixed algorithm with hooks (`hasHeader`, `validate`) is the point here. |
| 5 | Observer | Behavioral | L5 | Overdue and fine events must reach several receivers (notices, activity log) without `LoanService` knowing them. | `LoanEvents`, `LoanListener`, `OverdueNoticeListener`, `ActivityLog` | `ObserverTest`, `LoanServiceTest` | Direct calls from `LoanService` to e-mail code: rejected, service coupled to infra. |
| 6 | Command | Behavioral | L5 | Room bookings and cancellations must be undoable (requirement 5.2). | `Command`, `ReserveRoomCommand`, `CancelRoomCommand`, `CommandHistory` | `CommandUndoTest` | Memento of the whole reservation table: rejected (ADR-0005). |

Architectural pattern (L14, not counted above): **Repository** — `Repository<T>`, `CatalogRepository`, `InMemoryRepository` (file implementation planned for M4); **Dependency Injection** via the composition root `App`.

## Combinations (L7)

None yet — the first combination (State + Strategy) is planned for M3.

## Removed or simplified in the pattern audit (week 7)

| What | Why | ADR |
|------|-----|-----|
| `DateProvider` interface (M2) | Only one implementation, `SimulatedDate`; the interface was speculative generality. Removed, services take `SimulatedDate`. | [ADR-0010](adr/0010-pattern-audit.md) |
| Fine decorators (planned) | Three decorators for one formula; replaced by one configurable `DailyCappedFine` strategy in M3. | [ADR-0010](adr/0010-pattern-audit.md) |
