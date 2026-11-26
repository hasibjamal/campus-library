package edu.campus.library.infra;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.member.Member;
import edu.campus.library.infra.persistence.RepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.campus.library.TestFixtures.staff;
import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One contract, run against both repository families (Abstract Factory).
 * If both pass, the service layer cannot tell them apart.
 */
abstract class RepositoryContractTest {
    protected Repository<Member> repo;

    protected abstract RepositoryFactory factory();

    @BeforeEach
    void setUp() {
        repo = factory().members();
    }

    @Test
    void saveFindAndListInInsertionOrder() {
        repo.save(student("M-1"));
        repo.save(staff("M-2"));
        assertEquals("M-1", repo.findById("M-1").orElseThrow().id());
        assertEquals(List.of("M-1", "M-2"), repo.findAll().stream().map(Member::id).toList());
    }

    @Test
    void saveWithSameIdReplaces() {
        repo.save(student("M-1"));
        Member changed = student("M-1");
        changed.deactivate();
        repo.save(changed);
        assertEquals(1, repo.findAll().size());
        assertFalse(repo.findById("M-1").orElseThrow().isActive());
    }

    @Test
    void deleteRemoves() {
        repo.save(student("M-1"));
        repo.deleteById("M-1");
        assertTrue(repo.findById("M-1").isEmpty());
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void factoryCreatesTheWholeFamily() {
        RepositoryFactory f = factory();
        assertTrue(f.loans().findAll().isEmpty());
        assertTrue(f.rooms().findAll().isEmpty());
        assertTrue(f.roomReservations().findAll().isEmpty());
        assertEquals("Catalogue", f.catalog().loadRoot().name());
    }
}
