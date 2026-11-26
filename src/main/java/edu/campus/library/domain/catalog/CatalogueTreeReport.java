package edu.campus.library.domain.catalog;

/** Visitor: prints the catalogue as an indented tree with book counts per section. */
public class CatalogueTreeReport implements CatalogVisitor<String> {
    private int depth = 0;

    @Override
    public String visitBook(Book book) {
        return "  ".repeat(depth) + "- " + book + "\n";
    }

    @Override
    public String visitSection(Section section) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(depth))
          .append("[").append(section.name()).append("] (")
          .append(section.books().count()).append(" books)\n");
        depth++;
        for (CatalogComponent child : section.children()) {
            sb.append(child.accept(this));
        }
        depth--;
        return sb.toString();
    }
}
