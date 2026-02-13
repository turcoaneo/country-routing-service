package com.ovidiu.countryrouting.routing;

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

    @InjectMocks
    private RouteFinder finder;

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
}
