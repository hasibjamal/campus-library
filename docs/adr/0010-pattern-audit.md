# ADR-0010: Pattern audit (checkpoint, week 7)

Status: accepted · Date: week 7

## Context
L7 requires auditing our own code for smells and over-engineering.

## Findings (code as of v0.2)
| Smell | Where | Action |
|-------|-------|--------|
| Speculative generality | `DateProvider` interface with one implementation (`SimulatedDate`) | **Removed in this audit.** Services take `SimulatedDate` directly. Extract an interface only if a second date source appears. |
| Duplicated conditionals | `LoanService` and `Loan` check `LoanStatus` in several methods (pick up, return, overdue, cancel) | Planned for M3: **State**. |
| Type-code conditional | Fine computed with `if (type == STUDENT) … else …` | Planned for M3: **Strategy**. |
| Two responsibilities in one class | `RoomBookingService` does overlap checks *and* staff-only access checks | Planned for M3: protection **Proxy**. |
| Possible over-engineering | Proposal: decorators for fine rate / grace / cap | **Rejected**: one configurable strategy is enough. |

## Decision
Every pattern that remains has a test and a register row; no class exists only to show a pattern.

## Consequences
Fewer classes; register updated (section "Removed in the pattern audit").
