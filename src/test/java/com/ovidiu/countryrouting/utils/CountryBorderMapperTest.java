package com.ovidiu.countryrouting.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CountryBorderMapperTest {

    @Test
    void testMappingLoadsAndTransformsCorrectly() throws Exception {
        CountryBorderMapper mapper = new CountryBorderMapper();
        Map<String, CountryBorderMapper.CountryCompact> map = mapper.loadAndTransform();

        assertFalse(map.isEmpty());
        assertTrue(map.containsKey("CZE"));
        assertTrue(map.containsKey("ITA"));

        var cze = map.get("CZE");
        assertEquals("CZ", cze.getCca2());
        assertEquals("203", cze.getCcn3());
        assertEquals("CZE", cze.getCca3());
        assertNotNull(cze.getBorders());
        assertTrue(cze.getBorders().contains("AUT"));
    }
}
