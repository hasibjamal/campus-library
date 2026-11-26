# ADR-0005: Command for undoable room booking

Status: accepted · Date: week 6

## Context
Requirement 5.2: an undo/history feature whose test proves the state returns exactly.

## Options
- (A) Memento: snapshot all reservations before each change, restore on undo.
- (B) Command: each action knows how to reverse itself; `CommandHistory` keeps a stack.

## Decision
B. Reserve ↔ cancel are exact inverses, so a command needs to remember only one reservation. A memento would copy the whole table each time and would also revert changes made by other users in between.

## Consequences
- `CommandUndoTest` compares a full `toString` snapshot before and after undo.
- A command that throws is not recorded.
- Commands call the proxy, so undo respects access rules (Command + Proxy).
- Loans are not undoable on purpose: a returned book with a fine is a financial record.
