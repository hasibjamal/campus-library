package edu.campus.library.infra.persistence;

import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.catalog.Section;

public class InMemoryCatalogRepository implements CatalogRepository {
    private Section root = new Section("Catalogue");

    @Override
    public Section loadRoot() {
        return root;
    }

    @Override
    public void saveRoot(Section newRoot) {
        this.root = newRoot;
    }
}
