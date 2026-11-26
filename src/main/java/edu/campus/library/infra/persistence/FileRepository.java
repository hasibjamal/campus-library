package edu.campus.library.infra.persistence;

import edu.campus.library.domain.Identifiable;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Second Repository implementation: same behaviour as the in-memory one,
 * but every change is written to a file so data survives a restart.
 * Services do not know which one they use.
 */
public class FileRepository<T extends Identifiable & Serializable> extends InMemoryRepository<T> {
    private final Path file;

    public FileRepository(Path file) {
        this.file = file;
        FileStore.<LinkedHashMap<String, T>>read(file).ifPresent(items::putAll);
    }

    @Override
    public T save(T entity) {
        T saved = super.save(entity);
        flush();
        return saved;
    }

    @Override
    public void deleteById(String id) {
        super.deleteById(id);
        flush();
    }

    private void flush() {
        FileStore.write(file, new LinkedHashMap<>(items));
    }
}
