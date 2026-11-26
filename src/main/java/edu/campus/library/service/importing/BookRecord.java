package edu.campus.library.service.importing;

/** One parsed line of an import file. */
public record BookRecord(String sectionPath, String title, String author, int year) {
}
