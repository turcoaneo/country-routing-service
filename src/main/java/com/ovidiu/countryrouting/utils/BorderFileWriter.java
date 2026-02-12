package com.ovidiu.countryrouting.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class BorderFileWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void writeBordersToFile(Map<String, CountryBorderMapper.CountryCompact> data, File outputFile)
            throws IOException {

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Cannot write empty border data");
        }

        // Sort countries by key
        Map<String, CountryBorderMapper.CountryCompact> sorted = new TreeMap<>(data);

        // Sort borders inside each country
        sorted.values().forEach(c -> {
            if (c.getBorders() != null) {
                c.getBorders().sort(String::compareTo);
            }
        });

        MAPPER.writerWithDefaultPrettyPrinter().writeValue(outputFile, sorted);
    }
}
