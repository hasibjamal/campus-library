package edu.campus.library.infra.persistence;

import edu.campus.library.domain.Identifiable;
import edu.campus.library.domain.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** First Repository implementation: a map in memory. Data is lost on exit. */
public class InMemoryRepository<T extends Identifiable> implements Repository<T> {
    protected final Map<String, T> items = new LinkedHashMap<>();

    @Override
    public T save(T entity) {
        items.put(entity.id(), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public void deleteById(String id) {
        items.remove(id);
    }
}
