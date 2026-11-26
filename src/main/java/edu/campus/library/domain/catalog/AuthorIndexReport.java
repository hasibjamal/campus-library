package edu.campus.library.domain.catalog;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Visitor: author index (author -> titles). Added as a new class only,
 * which shows the report extension point is open for extension.
 */
public class AuthorIndexReport implements CatalogVisitor<Map<String, TreeSet<String>>> {
    private final Map<String, TreeSet<String>> index = new TreeMap<>();

    @Override
    public Map<String, TreeSet<String>> visitBook(Book book) {
        index.computeIfAbsent(book.author(), a -> new TreeSet<>()).add(book.title());
        return index;
    }

    @Override
    public Map<String, TreeSet<String>> visitSection(Section section) {
        section.children().forEach(c -> c.accept(this));
        return index;
    }
}
