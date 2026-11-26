package edu.campus.library.service;

import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.catalog.CatalogVisitor;
import edu.campus.library.domain.catalog.Section;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CatalogService {
    private final CatalogRepository repository;
    private final IdGenerator ids;

    public CatalogService(CatalogRepository repository, IdGenerator ids) {
        this.repository = repository;
        this.ids = ids;
    }

    public Section root() {
        return repository.loadRoot();
    }

    public Optional<Book> findBook(String bookId) {
        return root().findBook(bookId);
    }

    public Book getBook(String bookId) {
        return findBook(bookId).orElseThrow(() -> new NoSuchElementException("No book " + bookId));
    }

    public List<Book> search(String text) {
        String q = text.toLowerCase(Locale.ROOT);
        return root().books()
                .filter(b -> b.title().toLowerCase(Locale.ROOT).contains(q)
                        || b.author().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    /**
     * Adds a book under a section path such as "Science/Computing".
     * Missing sections are created.
     */
    public Book addBook(String sectionPath, String title, String author, int year) {
        Section root = root();
        Section target = root;
        for (String part : sectionPath.split("/")) {
            if (part.isBlank()) {
                continue;
            }
            Section current = target;
            target = current.subSection(part).orElseGet(() -> {
                Section created = new Section(part.trim());
                current.add(created);
                return created;
            });
        }
        Book book = new Book(ids.next("B"), title, author, year);
        target.add(book);
        repository.saveRoot(root);
        return book;
    }

    public <R> R report(CatalogVisitor<R> visitor) {
        return root().accept(visitor);
    }
}
