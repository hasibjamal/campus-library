package edu.campus.library.domain;

/** Every entity stored in a {@link Repository} has a stable string id. */
public interface Identifiable {
    String id();
}
