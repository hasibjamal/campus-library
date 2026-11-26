package edu.campus.library.service.importing;

import java.util.ArrayList;
import java.util.List;

/** CSV export from the old library system: section,title,author,year; quotes allowed. */
public class CsvBookImporter extends CatalogImporter {

    @Override
    public String format() {
        return "csv";
    }

    @Override
    protected BookRecord parse(String line) {
        return record(split(line));
    }

    /** Splits on commas outside double quotes ("Patterns, 2nd ed" stays one field). */
    static String[] split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields.toArray(String[]::new);
    }
}
