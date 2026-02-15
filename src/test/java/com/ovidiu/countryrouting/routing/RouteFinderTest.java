package com.ovidiu.countryrouting.routing;

import com.ovidiu.countryrouting.fuzzymatching.CountryCodeResolver;
import com.ovidiu.countryrouting.graph.GraphBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteFinderTest {

    @Mock
    private GraphBuilder graphBuilder;

    @Mock
    private CountryCodeResolver resolver;

    @InjectMocks
    private RouteFinder finder;

    // ---------------------------------------------------------
    // STRICT ROUTING TESTS (unchanged)
    // ---------------------------------------------------------

    @Test
    void testCzeToIta() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "CZE", List.of("AUT"),
                        "AUT", List.of("ITA"),
                        "ITA", List.of()
                )
        );

        List<String> route = finder.findShortestRoute("CZE", "ITA");
        assertNotNull(route);
        assertEquals(List.of("CZE", "AUT", "ITA"), route);
    }

    @Test
    void testFraToEsp() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "FRA", List.of("ESP"),
                        "ESP", List.of()
                )
        );

        List<String> route = finder.findShortestRoute("FRA", "ESP");
        assertNotNull(route);
        assertEquals(List.of("FRA", "ESP"), route);
    }

    @Test
    void testUsaToCan() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "USA", List.of("CAN"),
                        "CAN", List.of()
                )
        );

        List<String> route = finder.findShortestRoute("USA", "CAN");
        assertNotNull(route);
        assertEquals(List.of("USA", "CAN"), route);
    }

    @Test
    void testNoRoute() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "USA", List.of("MEX"),
                        "MEX", List.of(),
                        "AUS", List.of()
                )
        );

        List<String> route = finder.findShortestRoute("USA", "AUS");
        assertNull(route);
    }

    // ---------------------------------------------------------
    // NEW FUZZY ROUTING TESTS
    // ---------------------------------------------------------

    @Test
    void testFuzzySpnToIta() {
        when(resolver.resolve("SPN")).thenReturn("ESP");
        when(resolver.resolve("ITL")).thenReturn("ITA");

        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "ESP", List.of("FRA"),
                        "FRA", List.of("ITA"),
                        "ITA", List.of()
                )
        );

        List<String> route = finder.findShortestRouteFuzzy("SPN", "ITL");
        assertNotNull(route);
        assertEquals(List.of("ESP", "FRA", "ITA"), route);
    }

    @Test
    void testFuzzyInvalidCountry() {
        when(resolver.resolve("XXX")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> finder.findShortestRouteFuzzy("XXX", "ITA"));
    }

    @Test
    void testFuzzyNoRoute() {
        when(resolver.resolve("ESP")).thenReturn("ESP");
        when(resolver.resolve("USA")).thenReturn("USA");

        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "ESP", List.of("FRA"),
                        "FRA", List.of(),
                        "USA", List.of()
                )
        );

        List<String> route = finder.findShortestRouteFuzzy("ESP", "USA");
        assertNull(route);
    }
}
