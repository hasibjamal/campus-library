# ADR-0003: Strategy for fines, combined with State

Status: accepted · Date: week 9

## Context
Students and staff have different fine rules (rate, grace days, cap), and the library expects more (amnesty weeks, rare books).

## Options
- (A) `if/switch` on `MemberType` inside the return logic.
- (B) `FineStrategy` interface; `FinePolicy` maps a member type to a strategy, configured in `App`.
- (C) Decorator chain (base rate → grace → cap).

## Decision
B. Today both rules have the same shape, so one class `DailyCappedFine` with two configurations (`student()`, `staff()`) is enough. A rule with a different shape will be a new class.
C rejected: three tiny decorators for one formula is over-engineering (L7).

**Combination with State**: `BorrowedState`/`OverdueState.returnItem` decide *that* a fine is due; the strategy decides *how much*.

## Consequences
- Fine rules are tested alone (`FineStrategyTest`).
- `FinePolicy` fails fast if a member type has no strategy.
