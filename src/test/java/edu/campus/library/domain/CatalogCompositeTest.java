package edu.campus.library.domain;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.catalog.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Composite: the catalogue tree. */
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
}
