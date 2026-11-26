# ADR-0001: Four layers with inward dependencies and one composition root

Status: accepted · Date: week 2

## Context
The brief requires `domain`, `service`, `infra`, `ui`. Without rules, UI code starts calling repositories and domain classes start writing files.

## Options
- (A) Packages by feature (`loan`, `room`, …) with no layer rules.
- (B) Packages by layer, dependencies inward, checked by ArchUnit; `App` wires everything.
- (C) A DI framework (Spring) for wiring.

## Decision
B. Rules (see `ArchitectureTest`):
- `domain` depends on nothing in the project.
- `service` depends only on `domain`. Ports it needs from the outside (`NotificationChannel`) are interfaces in `service`.
- `infra` implements ports from `domain`/`service`; only `App` uses `infra`.
- `ui` sees only `service` (the facade) and `domain` types for display.
- No package cycles.

C rejected: the patterns must stay visible in our code and the app is small.

## Consequences
- Swapping storage or adding a channel never touches services.
- `App.build` is long, but it is the single place that shows the whole object graph.
