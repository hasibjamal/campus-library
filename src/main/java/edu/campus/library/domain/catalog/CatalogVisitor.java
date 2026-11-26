package edu.campus.library.domain.catalog;

/**
 * Visitor pattern (L13). A new report over the catalogue is a new class
 * implementing this interface; Book and Section never change.
 */
public interface CatalogVisitor<R> {
    R visitBook(Book book);

    R visitSection(Section section);
}
