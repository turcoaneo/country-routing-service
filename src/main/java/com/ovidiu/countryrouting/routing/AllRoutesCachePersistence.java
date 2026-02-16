package com.ovidiu.countryrouting.routing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AllRoutesCachePersistence {

    private static final Path FILE = Paths.get("all-routes-cache.json");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, AllRoutesCacheEntry> load() {
        if (!Files.exists(FILE)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(
                    Files.readAllBytes(FILE),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            // On error, start with empty cache
            return new HashMap<>();
        }
    }

    public void save(Map<String, AllRoutesCacheEntry> cache) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(FILE.toFile(), cache);
        } catch (IOException e) {
            // You might want to log this in real code
        }
    }
}
