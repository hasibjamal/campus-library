# ADR-0004: Observer for loan events; NotificationChannel as extension point

Status: accepted · Date: week 5

## Context
When a loan becomes overdue or a fine is charged, the reader must be notified, and the librarian wants an activity log. Calling e-mail code from `LoanService` would tie the service to infra and grow with every new channel.

## Options
- (A) `LoanService` calls `EmailSender` and `ActivityLog` directly.
- (B) `LoanService` publishes `LoanEvent`s to `LoanEvents`; listeners subscribe.
- (C) A library event bus.

## Decision
B. Two listeners: `OverdueNoticeListener` and `ActivityLog`. The notice listener sends through every `NotificationChannel` it is given.
C rejected: hides the pattern and would need its own ADR (brief §12).

## Consequences
- **Extension point**: a new channel (for example SMS) is a new class plus one line in `App`; no existing class changes.
- Listeners run synchronously in publish order; a failing channel would stop the others (acceptable for console demo; revisit if a real gateway is added).
