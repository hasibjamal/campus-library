# ADR-0007: Chain of Responsibility for loan eligibility

Status: accepted · Date: week 12

## Context
Before a loan opens: member active? fines under limit? under loan limit? book free? The library asks for more rules each term (e.g. "no loans during exam week for rare books").

## Options
- (A) One `canBorrow` method with `if`s.
- (B) A chain of `EligibilityCheck` objects; order set in `App`.
- (C) A list of predicates looped over.

## Decision
B. Each check has its own message and test, and the order is explicit (the cheapest and most important refusal comes first). C would work too but gives no place for per-rule configuration such as the fine limit.

## Consequences
- `LoanService` collects data once into `LoanRequest`, so checks are pure and need no repositories.
- A new rule is a new subclass and one `.then(...)` in `App`.
