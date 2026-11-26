# ADR-0006: Repository interface, two implementations, chosen by Abstract Factory

Status: accepted · Date: week 12

## Context
The brief requires a second persistence implementation switchable without touching services. We have five repositories (members, loans, rooms, room reservations, catalogue).

## Options
- (A) SQLite through JDBC.
- (B) File storage with Java serialization.
- (C) Choose each repository separately in `App`.
- (D) `RepositoryFactory` (Abstract Factory) with an in-memory and a file family.

## Decision
B + D. File storage needs no driver and no schema; entities are already `Serializable`. Writes go to a temp file and are moved, so a crash never leaves half a file.
D guarantees all repositories come from the same family; `App.main` picks it from `--store=file`.
A remains possible later: a `SqliteRepositoryFactory` is a new class.

## Consequences
- `RepositoryContractTest` runs the same tests on both families.
- Serialization ties the data files to class versions (`serialVersionUID` is set on every entity).
- Whole-file rewrite per save is fine for a campus library, not for millions of rows.
