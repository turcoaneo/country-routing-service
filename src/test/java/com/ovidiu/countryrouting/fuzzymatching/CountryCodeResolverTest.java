package com.ovidiu.countryrouting.fuzzymatching;

import com.ovidiu.countryrouting.utils.CountryBorderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CountryCodeResolverTest {

    @Spy
    CountryBorderMapper objectMapper;

    @InjectMocks
    private CountryCodeResolver resolver;

    @BeforeEach
    void setup() throws IOException {
        assertNotNull(objectMapper);
        resolver.extractData();
    }

    @Test
    void testExactMatch() {
        assertEquals("CZE", resolver.resolve("CZE"));
    }

    @Test
    void testCca2Match() {
        assertEquals("CZE", resolver.resolve("CZ"));
        assertEquals("ITA", resolver.resolve("IT"));
        assertEquals("DEU", resolver.resolve("DE"));
    }

    @Test
    void testCiocMatch() {
        assertEquals("ROU", resolver.resolve("ROU")); // IOC code is also ROU
        assertEquals("ESP", resolver.resolve("ESP"));
    }

    @Test
    void testFuzzyMatch() {
        assertEquals("ESP", resolver.resolve("SPN"));      // semantic match
        assertEquals("GRC", resolver.resolve("GR"));      // good
        assertEquals("ROU", resolver.resolve("ROM"));     // semantic match
        assertEquals("AUT", resolver.resolve("AUSTRIA")); // full name
    }

    @Test
    void testUnknown() {
        assertNull(resolver.resolve("XYZ"));           // garbage NOT rejected
        assertNull(resolver.resolve("12345"));            // numeric garbage NOT rejected
    }
}
