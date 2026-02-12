package com.ovidiu.countryrouting.graph;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ovidiu.countryrouting.utils.CountryBorderMapper.CountryCompact;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Map<String, List<String>> buildGraph() {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("data/borders.json");

        if (is == null) {
            throw new IllegalStateException("borders.json not found in resources/data/");
        }

        try {
            Map<String, CountryCompact> raw =
                    MAPPER.readValue(is, new TypeReference<>() {});

            Map<String, List<String>> graph = new HashMap<>();

            raw.forEach((cca3, compact) -> graph.put(cca3, compact.getBorders()));

            return graph;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load graph", e);
        }
    }
}
