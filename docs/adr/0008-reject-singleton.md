# ADR-0008: Rejected — Singleton

Status: accepted (rejected pattern) · Date: week 9

## Context
Several objects must exist once: `LoanEvents`, `SimulatedDate`, the repository set, `CommandHistory`. Singleton is the obvious textbook answer (L9).

## Options
- (A) `getInstance()` singletons.
- (B) Create one instance in `App` and pass it through constructors.

## Decision
B. A singleton is a hidden global: tests would share event listeners and the date between test cases, and the dependency would not be visible in constructors. The brief forbids singletons as hidden globals.

## Consequences
- Constructors are longer, but every dependency is visible.
- Tests build a fresh world each time (`LibraryWorld`, `App.build`).
