package com.ovidiu.countryrouting.routing;

import com.ovidiu.countryrouting.graph.GraphBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RouteFinderTest {

    private static RouteFinder finder;

    @BeforeAll
    static void setup() {
        GraphBuilder builder = new GraphBuilder();
        Map<String, List<String>> graph = builder.buildGraph();
        finder = new RouteFinder(graph);
    }

    @Test
    void testCzeToIta() {
        List<String> route = finder.findShortestRoute("CZE", "ITA");
        assertNotNull(route);
        assertEquals(List.of("CZE", "AUT", "ITA"), route);
    }

    @Test
    void testFraToEsp() {
        List<String> route = finder.findShortestRoute("FRA", "ESP");
        assertNotNull(route);
        assertEquals(List.of("FRA", "ESP"), route);
    }

    @Test
    void testUsaToCan() {
        List<String> route = finder.findShortestRoute("USA", "CAN");
        assertNotNull(route);
        assertEquals(List.of("USA", "CAN"), route);
    }

    @Test
    void testNoRoute() {
        List<String> route = finder.findShortestRoute("USA", "AUS");
        assertNull(route);
    }
}
