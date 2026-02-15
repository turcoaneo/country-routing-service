package com.ovidiu.countryrouting.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BordersJsonGeneratorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testBordersJsonIsGeneratedInResources() throws Exception {
        BordersJsonGenerator generator = new BordersJsonGenerator();
        generator.generateBordersJson();

        URL resourceUrl = getClass().getClassLoader().getResource("data/borders.json");
        assertNotNull(resourceUrl);

        File file = Paths.get(resourceUrl.toURI()).toFile();
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        Map<String, CountryBorderMapper.CountryCompact> loaded =
                MAPPER.readValue(file, new TypeReference<>() {
                });

        assertTrue(loaded.containsKey("CZE"));
        assertTrue(loaded.containsKey("ITA"));
        assertEquals("CZE", loaded.get("CZE").getCca3());

        // NEW: verify names exist
        var cze = loaded.get("CZE");
        assertNotNull(cze.getNames());
        assertFalse(cze.getNames().isEmpty());

        // Czech Republic should have at least one of these
        assertTrue(
                cze.getNames().stream().anyMatch(n -> n.equalsIgnoreCase("Czech Republic")) ||
                        cze.getNames().stream().anyMatch(n -> n.equalsIgnoreCase("Czechia"))
        );
    }

    @Test
    void testBordersJsonMatchesCountriesJsonForRandomSample() throws Exception {
        CountryBorderMapper mapper = new CountryBorderMapper();
        Map<String, CountryBorderMapper.CountryCompact> original = mapper.loadAndTransform();

        ObjectMapper om = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/borders.json");
        Map<String, CountryBorderMapper.CountryCompact> borders =
                om.readValue(is, new TypeReference<>() {});

        assertEquals(original.size(), borders.size());

        List<String> keys = new ArrayList<>(original.keySet());
        Collections.shuffle(keys);

        for (int i = 0; i < 10; i++) {
            String key = keys.get(i);
            var o = original.get(key);
            var b = borders.get(key);

            assertEquals(o.getCca2(), b.getCca2());
            assertEquals(o.getCcn3(), b.getCcn3());
            assertEquals(o.getCca3(), b.getCca3());
            assertEquals(o.getCioc(), b.getCioc());
            assertEquals(sorted(o.getBorders()), sorted(b.getBorders()));

            // NEW: names must match
            assertEquals(sorted(o.getNames()), sorted(b.getNames()));
        }
    }

    private List<String> sorted(List<String> list) {
        List<String> copy = new ArrayList<>(list);
        Collections.sort(copy);
        return copy;
    }
}
