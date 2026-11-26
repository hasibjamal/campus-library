# ADR-0012: Template Method for catalogue import

Status: accepted · Date: week 6

## Context
The library receives book lists as CSV (old system: header row, quoted commas) and TSV (faculty spreadsheet: no header). Both imports read lines, skip a header, validate, add the book and collect errors; only parsing differs.

## Options
- (A) Two independent importer classes (duplicated loop).
- (B) Template Method: `CatalogImporter.importFrom` is final and fixes the steps; subclasses implement `parse` and may override the hooks `hasHeader` and `validate`.
- (C) One importer class taking a parser Strategy.

## Decision
B. The algorithm (and its error handling) must be identical for every format, which is exactly what a final template method guarantees. C would allow the same, but the hooks for header and validation fit inheritance naturally and keep each format in one class.

## Consequences
- New format (JSON, MARC) = one subclass + register it in `App`.
- A bad line never aborts the import; it is reported with its line number.
