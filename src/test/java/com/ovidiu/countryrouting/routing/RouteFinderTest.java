package com.ovidiu.countryrouting.routing;

import com.github.benmanes.caffeine.cache.Cache;
import com.ovidiu.countryrouting.fuzzymatching.CountryCodeResolver;
import com.ovidiu.countryrouting.graph.GraphBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteFinderTest {

    @Mock
    private GraphBuilder graphBuilder;

    @Mock
    private CountryCodeResolver resolver;

    @Mock
    private Cache<String, AllRoutesCacheEntry> allRoutesCache;

    @Mock
    private AllRoutesCachePersistence persistence;

    @InjectMocks
    private RouteFinder finder;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(finder, "allRoutesCache", allRoutesCache);
        ReflectionTestUtils.setField(finder, "persistence", persistence);
    }

    // STRICT ROUTING TESTS
    @Test
    void testCzeToIta() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("CZE", List.of("AUT"), "AUT", List.of("ITA"), "ITA", List.of())
        );

        List<String> route = finder.findShortestRoute("CZE", "ITA");
        assertEquals(List.of("CZE", "AUT", "ITA"), route);
    }

    @Test
    void testFraToEsp() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("FRA", List.of("ESP"), "ESP", List.of())
        );

        List<String> route = finder.findShortestRoute("FRA", "ESP");
        assertEquals(List.of("FRA", "ESP"), route);
    }

    @Test
    void testUsaToCan() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("USA", List.of("CAN"), "CAN", List.of())
        );

        List<String> route = finder.findShortestRoute("USA", "CAN");
        assertEquals(List.of("USA", "CAN"), route);
    }

    @Test
    void testNoRoute() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("USA", List.of("MEX"), "MEX", List.of(), "AUS", List.of())
        );

        assertNull(finder.findShortestRoute("USA", "AUS"));
    }

    // FUZZY ROUTING TESTS
    @Test
    void testFuzzySpnToIta() {
        when(resolver.resolve("SPN")).thenReturn("ESP");
        when(resolver.resolve("ITL")).thenReturn("ITA");

        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("ESP", List.of("FRA"), "FRA", List.of("ITA"), "ITA", List.of())
        );

        List<String> route = finder.findShortestRouteFuzzy("SPN", "ITL");
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
                Map.of("ESP", List.of("FRA"), "FRA", List.of(), "USA", List.of())
        );

        assertNull(finder.findShortestRouteFuzzy("ESP", "USA"));
    }

    // ALL ROUTES (FUZZY) TESTS
    @Test
    void testAllRoutesFuzzyBasic() {
        when(resolver.resolve("SPN")).thenReturn("ESP");
        when(resolver.resolve("ITA")).thenReturn("ITA");

        when(allRoutesCache.getIfPresent("ESP->ITA")).thenReturn(null);

        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("ESP", List.of("FRA"), "FRA", List.of("ITA"), "ITA", List.of())
        );

        List<List<String>> routes =
                finder.findAllRoutesFuzzy("SPN", "ITA", 5, 10);

        assertEquals(1, routes.size());
        assertEquals(List.of("ESP", "FRA", "ITA"), routes.getFirst());

        verify(allRoutesCache).put(eq("ESP->ITA"), any());
    }

    @Test
    void testAllRoutesFuzzyInvalid() {
        when(resolver.resolve("XXX")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> finder.findAllRoutesFuzzy("XXX", "ITA", 5, 10));
    }

    @Test
    void testAllRoutesFuzzyNoRoute() {
        when(resolver.resolve("ESP")).thenReturn("ESP");
        when(resolver.resolve("USA")).thenReturn("USA");

        when(allRoutesCache.getIfPresent("ESP->USA")).thenReturn(null);

        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("ESP", List.of("FRA"), "FRA", List.of(), "USA", List.of())
        );

        List<List<String>> routes =
                finder.findAllRoutesFuzzy("ESP", "USA", 5, 10);

        assertTrue(routes.isEmpty());
    }

    @Test
    void testAllRoutesFuzzyUsesCacheWhenSmallerMDMR() {
        when(resolver.resolve("SPN")).thenReturn("ESP");
        when(resolver.resolve("ITA")).thenReturn("ITA");

        AllRoutesCacheEntry cached = new AllRoutesCacheEntry(
                10, 100,
                List.of(List.of("ESP", "FRA", "ITA"))
        );

        when(allRoutesCache.getIfPresent("ESP->ITA")).thenReturn(cached);

        List<List<String>> routes =
                finder.findAllRoutesFuzzy("SPN", "ITA", 5, 1);

        assertEquals(1, routes.size());
        assertEquals(List.of("ESP", "FRA", "ITA"), routes.getFirst());

        verify(graphBuilder, never()).buildGraph();
    }

    @Test
    void testAllRoutesFuzzyUsesReverseCache() {
        when(resolver.resolve("ESP")).thenReturn("ESP");
        when(resolver.resolve("ITA")).thenReturn("ITA");

        AllRoutesCacheEntry reverseCached = new AllRoutesCacheEntry(
                10, 100,
                List.of(List.of("ITA", "FRA", "ESP"))
        );

        when(allRoutesCache.getIfPresent("ESP->ITA")).thenReturn(null);
        when(allRoutesCache.getIfPresent("ITA->ESP")).thenReturn(reverseCached);

        List<List<String>> routes =
                finder.findAllRoutesFuzzy("ESP", "ITA", 5, 10);

        assertEquals(1, routes.size());
        assertEquals(List.of("ESP", "FRA", "ITA"), routes.getFirst());

        verify(graphBuilder, never()).buildGraph();
        verify(allRoutesCache).put(eq("ESP->ITA"), any());
    }

    @Test
    void testAllRoutesFuzzyRecomputesWhenLargerMDMR() {
        when(resolver.resolve("SPN")).thenReturn("ESP");
        when(resolver.resolve("ITA")).thenReturn("ITA");

        AllRoutesCacheEntry cached = new AllRoutesCacheEntry(
                5, 10,
                List.of(List.of("ESP", "FRA", "ITA"))
        );

        when(allRoutesCache.getIfPresent("ESP->ITA")).thenReturn(cached);

        when(graphBuilder.buildGraph()).thenReturn(
                Map.of("ESP", List.of("FRA"), "FRA", List.of("ITA"), "ITA", List.of())
        );

        List<List<String>> routes =
                finder.findAllRoutesFuzzy("SPN", "ITA", 10, 50);

        assertEquals(1, routes.size());
        assertEquals(List.of("ESP", "FRA", "ITA"), routes.getFirst());

        verify(graphBuilder).buildGraph();
        verify(allRoutesCache).put(eq("ESP->ITA"), any());
    }
}
