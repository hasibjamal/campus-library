package edu.campus.library.service;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogAndMemberServiceTest {
    private LibraryWorld w;

    @BeforeEach
    void setUp() {
        w = new LibraryWorld(new InMemoryRepositoryFactory());
    }

    @Test
    void addBookCreatesMissingSections() {
        Book b = w.catalog.addBook("Science/Computing", "Refactoring", "Fowler", 1999);
        assertTrue(b.id().startsWith("B-"));
        assertEquals("Refactoring",
                w.catalog.root().subSection("Science").orElseThrow()
                        .subSection("Computing").orElseThrow().findBook(b.id()).orElseThrow().title());
        w.catalog.addBook("Science", "Cosmos", "Sagan", 1980);
        assertEquals(1, w.catalog.root().children().stream()
                .filter(c -> c.name().equals("Science")).count(), "existing section reused");
    }

    @Test
    void searchMatchesTitleOrAuthorIgnoringCase() {
        assertEquals(1, w.catalog.search("book 3").size());
        assertEquals(1, w.catalog.search("AUTHOR 5").size());
        assertEquals(5, w.catalog.search("book").size());
        assertThrows(NoSuchElementException.class, () -> w.catalog.getBook("nope"));
    }

    @Test
    void treeTextShowsSectionsAndBooks() {
        w.catalog.addBook("Science/Computing", "Refactoring", "Fowler", 1999);
        String tree = w.catalog.treeText();
        assertTrue(tree.startsWith("[Catalogue] (6 books)"));
        assertTrue(tree.contains("    [Computing] (1 books)"));
        assertTrue(tree.contains("  - B-1 \"Book 1\""));
    }

    @Test
    void registerAndPayFine() {
        Member m = w.members.register("New Reader", "new@campus.edu", MemberType.STUDENT);
        assertEquals(m.id(), w.members.get(m.id()).id());
        m.addFine(new BigDecimal("4.00"));
        w.members.save(m);
        assertEquals(new BigDecimal("4.00"), w.members.payFine(m.id(), new BigDecimal("4")));
        assertEquals(3, w.members.all().size());
        assertThrows(NoSuchElementException.class, () -> w.members.get("M-404"));
    }

    @Test
    void simulatedDateOnlyMovesForward() {
        SimulatedDate d = new SimulatedDate(java.time.LocalDate.of(2026, 1, 1));
        d.advanceDays(3);
        assertEquals(java.time.LocalDate.of(2026, 1, 4), d.today());
        assertThrows(IllegalArgumentException.class, () -> d.advanceDays(-1));
    }
}
