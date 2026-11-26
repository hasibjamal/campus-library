package edu.campus.library.domain.catalog;

import edu.campus.library.domain.Identifiable;

import java.util.Objects;
import java.util.stream.Stream;

/** Leaf of the catalogue composite. One physical copy per Book in this version. */
public final class Book implements CatalogComponent, Identifiable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String title;
    private final String author;
    private final int year;

    public Book(String id, String title, String author, int year) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.author = Objects.requireNonNull(author);
        this.year = year;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return title;
    }

    public String title() {
        return title;
    }

    public String author() {
        return author;
    }

    public int year() {
        return year;
    }

    @Override
    public Stream<Book> books() {
        return Stream.of(this);
    }

    @Override
    public String toString() {
        return id + " \"" + title + "\" by " + author + " (" + year + ")";
    }
}
