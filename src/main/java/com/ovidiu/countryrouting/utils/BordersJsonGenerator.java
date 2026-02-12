package com.ovidiu.countryrouting.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class BordersJsonGenerator {

    private final CountryBorderMapper mapper = new CountryBorderMapper();
    private final BorderFileWriter writer = new BorderFileWriter();

    /**
     * Generates borders.json inside src/main/resources/data/
     */
    public void generateBordersJson() throws IOException {
        Map<String, CountryBorderMapper.CountryCompact> data = mapper.loadAndTransform();

        URL resourceUrl = getClass().getClassLoader().getResource("data");
        if (resourceUrl == null) {
            throw new IllegalStateException("Folder src/main/resources/data/ not found");
        }

        Path folderPath;
        try {
            folderPath = Paths.get(resourceUrl.toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve resource folder path", e);
        }

        File outputFile = folderPath.resolve("borders.json").toFile();
        writer.writeBordersToFile(data, outputFile);
    }
}