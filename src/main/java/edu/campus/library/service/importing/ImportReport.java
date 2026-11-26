package edu.campus.library.service.importing;

import java.util.List;

/** Result of an import: how many books were added and which lines failed. */
public record ImportReport(int imported, List<String> errors) {
}
