package edu.campus.library.domain;

import edu.campus.library.domain.catalog.AuthorIndexReport;
import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.catalog.CatalogueTreeReport;
import edu.campus.library.domain.catalog.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Composite (catalogue tree) combined with Visitor (reports). */
class CatalogCompositeTest {
    private Section root;
    private Section computing;

    @BeforeEach
    void setUp() {
        root = new Section("Catalogue");
        Section science = new Section("Science");
        computing = new Section("Computing");
        computing.add(new Book("B-1", "Design Patterns", "Gamma", 1994));
        computing.add(new Book("B-2", "Refactoring", "Fowler", 1999));
        science.add(computing).add(new Book("B-3", "Cosmos", "Sagan", 1980));
        root.add(science).add(new Book("B-4", "Analysis Patterns", "Fowler", 1996));
    }

    @Test
    void sectionAndBookAreTreatedUniformly() {
        assertEquals(4, root.books().count());
        assertEquals(2, computing.books().count());
        assertEquals(1, new Book("X", "t", "a", 2000).books().count());
    }

    @Test
    void findsBookDeepInTheTree() {
        assertEquals("Refactoring", root.findBook("B-2").orElseThrow().title());
        assertFalse(root.findBook("B-99").isPresent());
    }

    @Test
    void findsSubSectionIgnoringCase() {
        Section science = root.subSection("science").orElseThrow();
        assertTrue(science.subSection("COMPUTING").isPresent());
        assertFalse(root.subSection("Computing").isPresent(), "only direct children");
    }

    @Test
    void childrenListIsReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> root.children().clear());
    }

    @Test
    void treeReportVisitsEveryNodeWithIndentation() {
        String report = root.accept(new CatalogueTreeReport());
        assertTrue(report.startsWith("[Catalogue] (4 books)"));
        assertTrue(report.contains("    [Computing] (2 books)"));
        assertTrue(report.contains("      - B-1 \"Design Patterns\""));
        assertTrue(report.contains("  - B-4 \"Analysis Patterns\""));
    }

    @Test
    void authorIndexGroupsTitlesByAuthor() {
        Map<String, TreeSet<String>> index = root.accept(new AuthorIndexReport());
        assertEquals(Set.of("Fowler", "Gamma", "Sagan"), index.keySet());
        assertEquals(new TreeSet<>(Set.of("Analysis Patterns", "Refactoring")), index.get("Fowler"));
    }
}
