package edu.campus.library.domain.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/** Composite node of the catalogue: a section that contains sub-sections and books. */
public final class Section implements CatalogComponent {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final ArrayList<CatalogComponent> children = new ArrayList<>();

    public Section(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String name() {
        return name;
    }

    public Section add(CatalogComponent child) {
        children.add(Objects.requireNonNull(child));
        return this;
    }

    public List<CatalogComponent> children() {
        return Collections.unmodifiableList(children);
    }

    /** Finds a direct child section by name. */
    public Optional<Section> subSection(String sectionName) {
        return children.stream()
                .filter(Section.class::isInstance)
                .map(Section.class::cast)
                .filter(s -> s.name.equalsIgnoreCase(sectionName))
                .findFirst();
    }

    public Optional<Book> findBook(String bookId) {
        return books().filter(b -> b.id().equals(bookId)).findFirst();
    }

    @Override
    public Stream<Book> books() {
        return children.stream().flatMap(CatalogComponent::books);
    }
}
