package edu.campus.library.ui;

import edu.campus.library.App;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import edu.campus.library.service.LibraryFacade;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static edu.campus.library.TestFixtures.DAY;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Scripted console session: the UI only drives the facade. */
class ConsoleMenuTest {

    private String run(String script) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);
        LibraryFacade lib = App.build(new InMemoryRepositoryFactory(), DAY, out);
        new ConsoleMenu(lib, new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), out).run();
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fullSession() {
        String out = run(String.join("\n",
                "1", "2", "patterns", "2", "zzz", "5",
                "6", "M-1", "B-1",
                "6", "M-2", "B-1",
                "7", "M-2", "B-2",
                "8", "L-1001",
                "18", "30", "11",
                "9", "L-1000",
                "10", "12", "M-1", "1",
                "13",
                "14", "M-1", "RM-1", "", "10", "12", "study",
                "16", "",
                "15", "R-1002",
                "17", "17", "17",
                "4", "Art", "Ways of Seeing", "John Berger", "1972",
                "19", "99", "0", ""));
        assertTrue(out.contains("[Computing] (3 books)"));
        assertTrue(out.contains("[available]"));
        assertTrue(out.contains("No books found."));
        assertTrue(out.contains("Borrowed: L-1000"));
        assertTrue(out.contains("! Book B-1 is already on loan or reserved"));
        assertTrue(out.contains("Picked up: L-1001"));
        assertTrue(out.contains("Flagged overdue: 2"));
        assertTrue(out.contains("Returned: L-1000"));
        assertTrue(out.contains("Paid 1.00"));
        assertTrue(out.contains("Booked: R-1002"));
        assertTrue(out.contains("Cancelled."));
        assertTrue(out.contains("Undone: cancel reservation R-1002"));
        assertTrue(out.contains("Nothing to undo."));
        assertTrue(out.contains("Added B-"));
        assertTrue(out.contains("Unknown option"));
        assertTrue(out.contains("Bye."));
    }

    @Test
    void importsBooksFromFile() throws java.io.IOException {
        java.nio.file.Path f = java.nio.file.Files.createTempFile("books", ".csv");
        java.nio.file.Files.writeString(f, "s,t,a,y\nArt,Ways of Seeing,John Berger,1972\nArt,bad\n");
        String out = run("20\ncsv\n" + f + "\n20\nxml\n" + f + "\n20\ncsv\n/no/such/file\n0\n");
        assertTrue(out.contains("Imported 1 books"));
        assertTrue(out.contains("skipped line 3"));
        assertTrue(out.contains("! Unknown import format xml"));
        assertTrue(out.contains("! Cannot read /no/such/file"));
    }

    @Test
    void endOfInputExitsCleanly() {
        assertTrue(run("").contains("Bye."));
    }

    @Test
    void emptyLoanListIsShown() {
        assertTrue(run("10\n0\n").contains("No loans."));
    }
}
