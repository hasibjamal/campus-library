package edu.campus.library.service.importing;

import edu.campus.library.service.CatalogService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Method (L6). The import algorithm is fixed here: read lines, skip
 * the header, parse, validate, add, collect errors. Subclasses supply only the
 * step that differs between file formats: {@link #parse(String)}.
 */
public abstract class CatalogImporter {

    /** The template method. Final so the steps and their order cannot change. */
    public final ImportReport importFrom(Reader source, CatalogService catalog) {
        int imported = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(source)) {
            String line;
            int lineNo = 0;
            while ((line = in.readLine()) != null) {
                lineNo++;
                if (line.isBlank() || (lineNo == 1 && hasHeader())) {
                    continue;
                }
                try {
                    BookRecord r = parse(line);
                    validate(r);
                    catalog.addBook(r.sectionPath(), r.title(), r.author(), r.year());
                    imported++;
                } catch (RuntimeException e) {
                    errors.add("line " + lineNo + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new ImportReport(imported, List.copyOf(errors));
    }

    /** Format name used to pick the importer, e.g. "csv". */
    public abstract String format();

    /** Variable step: turn one line into a record. */
    protected abstract BookRecord parse(String line);

    /** Hook: formats without a header row override this. */
    protected boolean hasHeader() {
        return true;
    }

    /** Hook with a sensible default; subclasses may add rules. */
    protected void validate(BookRecord r) {
        if (r.title().isBlank() || r.author().isBlank() || r.sectionPath().isBlank()) {
            throw new IllegalArgumentException("section, title and author are required");
        }
        if (r.year() < 1450 || r.year() > Year.now().getValue() + 1) {
            throw new IllegalArgumentException("implausible year " + r.year());
        }
    }

    /** Shared helper for subclasses. */
    protected static BookRecord record(String[] fields) {
        if (fields.length != 4) {
            throw new IllegalArgumentException("expected 4 fields but found " + fields.length);
        }
        try {
            return new BookRecord(fields[0].trim(), fields[1].trim(), fields[2].trim(),
                    Integer.parseInt(fields[3].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("year is not a number: " + fields[3].trim());
        }
    }
}
