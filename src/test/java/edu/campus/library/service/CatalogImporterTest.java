package edu.campus.library.service;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import edu.campus.library.service.importing.CsvBookImporter;
import edu.campus.library.service.importing.ImportReport;
import edu.campus.library.service.importing.TsvBookImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Template Method: same algorithm, two formats; bad lines are reported, not fatal. */
class CatalogImporterTest {
    private LibraryWorld w;

    @BeforeEach
    void setUp() {
        w = new LibraryWorld(new InMemoryRepositoryFactory());
    }

    @Test
    void csvSkipsHeaderAndHandlesQuotedCommas() {
        String csv = "section,title,author,year\n"
                + "Science/Computing,\"Patterns, 2nd \"\"ed\"\"\",Gamma,1995\n"
                + "\n"
                + "Art,Ways of Seeing,John Berger,1972\n";
        ImportReport r = new CsvBookImporter().importFrom(new StringReader(csv), w.catalog);
        assertEquals(2, r.imported());
        assertTrue(r.errors().isEmpty());
        List<Book> found = w.catalog.search("Patterns, 2nd");
        assertEquals("Patterns, 2nd \"ed\"", found.get(0).title());
        assertEquals(1, w.catalog.root().subSection("Art").orElseThrow().books().count());
    }

    @Test
    void tsvHasNoHeader() {
        String tsv = "Humanities\tSPQR\tMary Beard\t2015\n";
        ImportReport r = new TsvBookImporter().importFrom(new StringReader(tsv), w.catalog);
        assertEquals(1, r.imported());
        assertEquals(1, w.catalog.search("SPQR").size());
    }

    @Test
    void invalidLinesAreCollectedWithLineNumbers() {
        String csv = "h\n"
                + "Art,Only three,fields\n"
                + "Art,Title,Author,notayear\n"
                + "Art,Title,Author,1200\n"
                + ",Title,Author,2000\n"
                + "Art,Good,Author,2001\n";
        ImportReport r = new CsvBookImporter().importFrom(new StringReader(csv), w.catalog);
        assertEquals(1, r.imported());
        assertEquals(4, r.errors().size());
        assertTrue(r.errors().get(0).startsWith("line 2: expected 4 fields"));
        assertTrue(r.errors().get(1).contains("not a number"));
        assertTrue(r.errors().get(2).contains("implausible year"));
        assertTrue(r.errors().get(3).contains("required"));
    }
}
