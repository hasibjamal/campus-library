# Building the history milestone by milestone

The repository history is graded, so do **not** push this code as one commit. Import it piece by piece, one issue → branch → PR → review per piece, rotating authors, and make sure each of you can explain what you merge.

| Milestone | Tag | Issues / PRs (suggested) |
|-----------|-----|--------------------------|
| M1 | v0.1 | pom + CI + VS Code config; ADR-0001 + `ArchitectureTest`; `docs/USER_STORIES.md`; README skeleton |
| M2 | v0.2 | domain entities + `Repository` + `InMemoryRepository`; catalogue Composite; Observer (`LoanEvents`, listeners, `EmailChannel`); Command undo for rooms; Abstract Factory (in-memory family); `LoanService` + `ConsoleMenu` + `App` end-to-end |
| Checkpoint | — | ADR-0010 audit; register update |
| M3 | v0.3 | State (`LoanState`…); Strategy (`FineStrategy`, `FinePolicy`) + combination; Builder for `RoomReservation`; Facade; Proxy; **`SmsChannel` as the extension-point PR** |
| M4 | v0.4 | Chain (`EligibilityCheck`…); Visitor reports; `FileRepository` + `FileRepositoryFactory`; coverage ≥ 60% |
| M5 | v1.0 | ADR-0008, ADR-0009, reflections, slides, `scripts/check-deliverables.sh` green |

Commit style: `feat: add LoanState machine (#12)`. Branches: `feature/12-loan-state`.
Tags: `git tag -a v0.1 -m "M1 foundation" && git push origin v0.1`.
