# ADR-0010: Pattern audit (checkpoint, week 7)

Status: accepted · Date: week 7

## Context
L7 requires auditing our own code for smells and over-engineering.

## Findings
| Smell | Where | Action |
|-------|-------|--------|
| Dead code (unused Strategy) | `NoFine` was written "for later", no reader type used it | **Removed.** Re-add as a new class when an amnesty rule is requested. |
| Speculative generality | A `DateProvider` interface was planned with only one implementation | **Not created**; concrete `SimulatedDate` is injected instead. Extract an interface only if a second source appears. |
| Possible over-engineering | Decorators for fine rate / grace / cap | **Not built**; one configurable `DailyCappedFine` (ADR-0003). |
| Duplicated conditionals | Loan status checks | Kept State (ADR-0002) — it removes real duplication. |

## Decision
Every pattern that remains has a test and a register row; no class exists only to show a pattern.

## Consequences
Fewer classes; register updated (section "Removed in the pattern audit").
