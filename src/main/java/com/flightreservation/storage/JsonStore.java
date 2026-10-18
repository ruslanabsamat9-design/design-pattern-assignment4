package com.flightreservation.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonStore {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path directory;

    public JsonStore(Path directory) {
        this.directory = directory;
        try { Files.createDirectories(directory); }
        catch (IOException e) { throw new IllegalStateException("Cannot create data directory", e); }
    }

    public synchronized <T> List<T> read(String file, TypeReference<List<T>> type) {
        Path path = directory.resolve(file);
        if (!Files.exists(path)) return new ArrayList<>();
        try { return mapper.readValue(path.toFile(), type); }
        catch (IOException e) { throw new IllegalStateException("Cannot read " + file, e); }
    }

    public synchronized void write(String file, Object value) {
        try { mapper.writerWithDefaultPrettyPrinter().writeValue(directory.resolve(file).toFile(), value); }
        catch (IOException e) { throw new IllegalStateException("Cannot write " + file, e); }
    }

    public Path getDirectory() { return directory; }
}
