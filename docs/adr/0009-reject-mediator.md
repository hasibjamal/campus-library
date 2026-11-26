# ADR-0009: Rejected — Mediator

Status: accepted (rejected pattern) · Date: week 12

## Context
Loan, member and notification logic interact; a `LibraryMediator` that all services talk through was proposed (L12).

## Options
- (A) Mediator between services.
- (B) Keep Observer for events, direct calls for the rest.

## Decision
B. Communication is one-directional (loan service → listeners). There are no many-to-many conversations among peers, which is what Mediator solves. A mediator here would become a god class that knows every service.

## Consequences
- If rooms, loans and events start reacting to each other (e.g. "block room booking while overdue"), reconsider.
