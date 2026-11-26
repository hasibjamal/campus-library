package edu.campus.library.service.importing;

/** Tab-separated list from the faculty spreadsheet; it has no header row. */
public class TsvBookImporter extends CatalogImporter {

    @Override
    public String format() {
        return "tsv";
    }

    @Override
    protected boolean hasHeader() {
        return false;
    }

    @Override
    protected BookRecord parse(String line) {
        return record(line.split("\t", -1));
    }
}
