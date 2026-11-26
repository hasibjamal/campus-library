package edu.campus.library.infra.persistence;

import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.catalog.Section;

import java.nio.file.Path;

public class FileCatalogRepository implements CatalogRepository {
    private final Path file;
    private Section root;

    public FileCatalogRepository(Path file) {
        this.file = file;
        this.root = FileStore.<Section>read(file).orElseGet(() -> new Section("Catalogue"));
    }

    @Override
    public Section loadRoot() {
        return root;
    }

    @Override
    public void saveRoot(Section newRoot) {
        this.root = newRoot;
        FileStore.write(file, newRoot);
    }
}
