package edu.campus.library.domain.catalog;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Composite pattern (L10): the catalogue is a tree of sections and books.
 * Clients treat a single book and a whole section the same way.
 */
public interface CatalogComponent extends Serializable {
    String name();

    /** All books at or below this node. */
    Stream<Book> books();
}
