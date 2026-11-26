# ADR-0011: Adapter for the mail provider SDK

Status: accepted · Date: week 4

## Context
Overdue notices go out by e-mail through a provider SDK (`QuickMailClient`, simulated). Its API — `post(address, htmlBody, headers)` returning a status code — is different from what our services want to call.

## Options
- (A) Call the SDK from `OverdueNoticeListener`.
- (B) An adapter `EmailChannel` implementing our `NotificationChannel` port and translating to the SDK.

## Decision
B. The service layer only knows `NotificationChannel`. The adapter builds the HTML body, sets the `Subject` header and turns a non-202 status into an exception.

## Consequences
- Changing provider = a new adapter; nothing in `service` changes.
- The SDK stays untouched (we do not own it).
- `EmailAdapterTest` checks the translation with a recording subclass of the SDK — no network.
