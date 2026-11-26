package edu.campus.library.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository pattern (L14): the service layer only talks to this interface.
 * Implementations live in the infra layer (in-memory and file).
 */
public interface Repository<T extends Identifiable> {
    T save(T entity);

    Optional<T> findById(String id);

    List<T> findAll();

    void deleteById(String id);
}
