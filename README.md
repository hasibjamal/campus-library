# Campus Library and Room Booking

A pattern-driven information system for a university library (Capstone, *Application Design Based on Patterns*, topic 1).

## Domain

The library has a **catalogue** organised as a tree of sections (Science → Computing → books).
**Members** are students or staff. A member can **reserve** a book and pick it up later, or **borrow** it at the desk.
Each loan moves through a lifecycle: *Reserved → Borrowed → (Overdue) → Returned*, or *Reserved → Cancelled*.
Before a loan is opened the system checks that the member is active, has no large unpaid fines, is under the loan limit, and that the book is free.
A daily **overdue scan** flags late loans and sends **overdue notices** by e-mail.
On return, a **fine** is charged according to the reader type (students: 0.50/day, cap 20; staff: 0.25/day after 2 grace days, cap 10).
Members can also **book study rooms** (08:00–22:00, max 3 hours, no double booking). Some rooms are **staff-only**. Room bookings and cancellations can be **undone**.

## How to run

Requirements: Java 17+, Maven 3.9+.

```bash
mvn verify                                        # tests + ArchUnit rules + 60% coverage gate
mvn -q compile exec:java                          # run (in-memory store)
```

Or `mvn package` and then `java -jar target/campus-library.jar`.
In VS Code press **F5** and pick *App (in-memory store)*.

### Demo script (main use case + extension point)

| Step | Menu input | What it shows |
|------|-----------|---------------|
| 1 | `1` | Catalogue tree (Composite) |
| 2 | `6`, `M-1`, `B-1` | Borrow passes the loan checks |
| 3 | `6`, `M-2`, `B-1` | Refused: book already on loan |
| 4 | `18`, `20` then `11` | Advance 20 days, overdue scan → e-mail notice (Observer, Adapter) |
| 5 | `9`, `L-1000` | Return → fine 3.00 |
| 6 | `14`, `M-1`, `RM-3`, … | Student refused staff-only room |
| 7 | `14`, `M-1`, `RM-1`, ``, `10`, `12`, `study` | Room booked (Command) |
| 8 | `17` | Undo the booking (Command) |
| 9 | `20`, `csv`, path | Import books from CSV (Template Method) |

Demo data: members `M-1`, `M-2` (students), `M-3` (staff); books `B-1`…`B-6`; rooms `RM-1`, `RM-2`, `RM-3` (staff only).

## Architecture

Four layers. Dependencies point inward; the rules are enforced by `ArchitectureTest` (ArchUnit).
`App` is the only composition root: it creates every concrete class and wires them together.

```mermaid
flowchart TB
    App["App (composition root)"]
    subgraph UI["ui"]
        Console[ConsoleMenu]
    end
    subgraph Infra["infra"]
        RF["RepositoryFactory<br/>InMemory"]
        Ch["EmailChannel (adapter)"]
    end
    subgraph Service["service"]
        LoanS[LoanService]
        CatS["CatalogService + CatalogImporter"]
        RoomS[RoomBookingService]
        Events[LoanEvents]
        Cmd[CommandHistory]
    end
    subgraph Domain["domain"]
        Loan[Loan]
        Cat["Section / Book"]
        Repo["Repository&lt;T&gt;"]
    end
    App --> Console & RF & Ch
    Console --> LoanS & CatS & RoomS & Cmd
    LoanS --> Events & Loan & Repo
    CatS --> Cat
    RF -. implements .-> Repo
    Ch -. implements .-> Service
```

| Package | Contents | May depend on |
|---------|----------|---------------|
| `domain` | Entities and domain rules, `Repository` interface | nothing else |
| `service` | Use cases, `NotificationChannel` port | domain |
| `infra` | Repository implementations, notification channels | service, domain |
| `ui` | Console menu | service, domain (for display) |
| root | `App` — composition root | everything |

## Patterns

6 patterns (1 creational, 2 structural, 3 behavioral). Details, classes and tests: [docs/PATTERN_REGISTER.md](docs/PATTERN_REGISTER.md). Decisions: [docs/adr](docs/adr).

## Extension points (Open/Closed)

| To add… | Write a new class implementing | Then register it in |
|---------|-------------------------------|---------------------|
| A notification channel (Telegram, push) | `service.NotificationChannel` | `App.build` (channel list) |
| An undoable room action | `service.command.Command` | `ConsoleMenu` |
| An import format (JSON, MARC) | extend `service.importing.CatalogImporter` | `App.build` (importer list) |
| A mail provider | adapter implementing `service.NotificationChannel` | `App.build` |
| A storage technology (SQLite) | `infra.persistence.RepositoryFactory` | `App.main` |


## Persistence

`Repository<T>` (domain) with an in-memory implementation, created through `RepositoryFactory`. A second, file-based implementation is planned for M4 and will be switched in `App` only.

## Libraries

| Library | Scope | Why |
|---------|-------|-----|
| JUnit 5 | test | unit tests |
| ArchUnit | test | layer rules |
| JaCoCo (plugin) | build | coverage gate ≥ 60% |
| exec-maven-plugin | build | `mvn exec:java` |

No library implements a pattern for us.

## Team

| Student | GitHub handle | Reflection |
|---------|---------------|------------|
| _name_ | _@handle_ | `docs/reflection-<handle>.md` |
