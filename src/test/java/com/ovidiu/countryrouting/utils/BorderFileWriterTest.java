package com.ovidiu.countryrouting.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BorderFileWriterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testBordersJsonIsWrittenCorrectly() throws Exception {
        CountryBorderMapper mapper = new CountryBorderMapper();
        Map<String, CountryBorderMapper.CountryCompact> data = mapper.loadAndTransform();

        File tempFile = Files.createTempFile("borders", ".json").toFile();

        BorderFileWriter writer = new BorderFileWriter();
        writer.writeBordersToFile(data, tempFile);

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);

        Map<String, CountryBorderMapper.CountryCompact> loaded =
                MAPPER.readValue(tempFile, new TypeReference<>() {});

        assertEquals(data.size(), loaded.size());
        assertTrue(loaded.containsKey("CZE"));
        assertTrue(loaded.containsKey("ITA"));

        var ita = loaded.get("ITA");
        assertEquals("ITA", ita.getCca3());
        assertNotNull(ita.getBorders());
    }
}