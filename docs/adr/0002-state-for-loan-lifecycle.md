# ADR-0002: State pattern for the loan lifecycle

Status: accepted · Date: week 8

## Context
A loan is Reserved, Borrowed, Overdue, Returned or Cancelled. Each action (pick up, return, mark overdue, cancel) is legal only in some states. With a status string, the checks were repeated in `LoanService` and the UI.

## Options
- (A) `enum Status` + `switch` in each method of `Loan`.
- (B) `LoanState` interface, one class per state; `Loan` delegates.

## Decision
B. Default methods in `LoanState` throw `IllegalStateException`, so each state class only overrides what it allows. Transitions live next to their rule.

## Consequences
- Five small classes instead of one `switch` per action.
- A new state (e.g. *Lost*) is a new class plus one transition in an existing state.
- Proof after v1.0: **renewal** (US-11) was one new default method in `LoanState` and one override in `BorrowedState`; no other state class changed.
- States are stateless and `Serializable`, so the file repository stores them.
