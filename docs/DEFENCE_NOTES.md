# Defence preparation

## Likely live change requests and where they go

| Request | New class | Edit (composition root only) | Test to write |
|---------|-----------|------------------------------|---------------|
| New notification channel (Telegram) | `infra.notification.TelegramChannel implements NotificationChannel` | add to `channels` list in `App.build` | send() output, like `NotificationChannelTest` |
| Amnesty week: no fines | `domain.fine.NoFine implements FineStrategy` | map it in `FinePolicy` in `App.build` | `fineFor(n) == 0.00` |
| New report: books per decade / CSV export | `domain.catalog.DecadeReport implements CatalogVisitor<…>` | call from `LibraryFacade` / menu | visit a small tree |
| New loan rule: max 1 loan per author | `service.eligibility.SameAuthorCheck extends EligibilityCheck` | add `.then(...)` in `App.build` | like `EligibilityChainTest` |
| SQLite storage | `infra.persistence.SqliteRepositoryFactory` | `App.main` flag | run `RepositoryContractTest` on it |
| New member type (GUEST) | new enum constant + a `FineStrategy` | `FinePolicy` map | **note:** this edits `MemberType` — explain why an enum is acceptable here |

## VS Code skills to show
F5 (launch configs in `.vscode/launch.json`), run one test / all tests from Test Explorer, breakpoint in `BorrowedState.returnItem` (State + Strategy meeting point), open a PR from the GitHub Pull Requests extension.

## Three patterns for the walk-through (suggested)
1. **State + Strategy** — `Loan`, `LoanState`, `BorrowedState.returnItem`, `FineStrategy`.
2. **Observer** + extension point — `LoanEvents`, `OverdueNoticeListener`, `SmsChannel`.
3. **Command** — `CommandHistory`, `CancelRoomCommand`, undo test.
