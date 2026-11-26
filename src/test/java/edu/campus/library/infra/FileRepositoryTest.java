package edu.campus.library.infra;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.catalog.Section;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.Repository;
import edu.campus.library.domain.fine.DailyCappedFine;
import edu.campus.library.infra.persistence.FileRepositoryFactory;
import edu.campus.library.infra.persistence.RepositoryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static edu.campus.library.TestFixtures.DAY;
import static edu.campus.library.TestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileRepositoryTest extends RepositoryContractTest {
    @TempDir
    Path dir;

    @Override
    protected RepositoryFactory factory() {
        return new FileRepositoryFactory(dir);
    }

    @Test
    void dataSurvivesReopening() {
        repo.save(student("M-1"));
        assertEquals(1, new FileRepositoryFactory(dir).members().findAll().size());
    }

    @Test
    void loanStateIsPersisted() {
        Repository<Loan> loans = factory().loans();
        Loan l = new Loan("L-1", "B-1", "M-1", DAY);
        l.pickUp(DAY, 14);
        l.markOverdue(DAY.plusDays(20));
        loans.save(l);
        Loan reloaded = new FileRepositoryFactory(dir).loans().findById("L-1").orElseThrow();
        assertEquals("OVERDUE", reloaded.stateName());
        reloaded.returnItem(DAY.plusDays(20), DailyCappedFine.student());
        assertEquals("RETURNED", reloaded.stateName());
    }

    @Test
    void catalogueTreeIsPersisted() {
        CatalogRepository c = factory().catalog();
        c.saveRoot(new Section("Catalogue").add(new Section("Art").add(new Book("B-1", "T", "A", 2000))));
        assertEquals(1, new FileRepositoryFactory(dir).catalog().loadRoot().books().count());
    }

    @Test
    void corruptFileIsReported() throws IOException {
        Files.writeString(dir.resolve("loans.dat"), "not a serialised object");
        assertThrows(UncheckedIOException.class, () -> new FileRepositoryFactory(dir).loans());
    }
}
