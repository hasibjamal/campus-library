package edu.campus.library.infra.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/** Small helper: reads and writes one serialised object, writing atomically via a temp file. */
final class FileStore {
    private FileStore() {
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<T> read(Path file) {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (InputStream in = Files.newInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(in)) {
            return Optional.of((T) ois.readObject());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Incompatible data file " + file, e);
        }
    }

    static void write(Path file, Serializable value) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp);
                 ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(value);
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }
}
