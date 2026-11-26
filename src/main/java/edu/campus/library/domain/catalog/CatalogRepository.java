package edu.campus.library.domain.catalog;

/** Stores the whole catalogue tree (the root section). */
public interface CatalogRepository {
    Section loadRoot();

    void saveRoot(Section root);
}
