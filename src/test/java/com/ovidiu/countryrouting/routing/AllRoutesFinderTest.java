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
class AllRoutesFinderTest {

    @Mock
    private GraphBuilder graphBuilder;

    @InjectMocks
    private AllRoutesFinder allRoutesFinder;

    @Test
    void testMultipleRoutes() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B", "C"),
                        "B", List.of("D"),
                        "C", List.of("D"),
                        "D", List.of()
                )
        );

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "D");

        assertEquals(2, routes.size());
        assertTrue(routes.contains(List.of("A", "B", "D")));
        assertTrue(routes.contains(List.of("A", "C", "D")));
    }

    @Test
    void testNoRoutes() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of(),
                        "C", List.of()
                )
        );

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "C");
        assertTrue(routes.isEmpty());
    }

    @Test
    void testOriginEqualsDestination() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of()
                )
        );

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "A");

        assertEquals(1, routes.size());
        assertEquals(List.of("A"), routes.getFirst());
    }

    @Test
    void testUnknownCountry() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of()
                )
        );

        assertThrows(IllegalArgumentException.class,
                () -> allRoutesFinder.findAllRoutes("X", "B"));
    }

    @Test
    void testGraphWithCycle() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of("C"),
                        "C", List.of("A") // cycle
                )
        );

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "C");

        assertEquals(1, routes.size());
        assertEquals(List.of("A", "B", "C"), routes.getFirst());
    }

    @Test
    void testIterativeMatchesRecursive() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B", "C"),
                        "B", List.of("D"),
                        "C", List.of("D"),
                        "D", List.of()
                )
        );

        List<List<String>> recursive = allRoutesFinder.findAllRoutes("A", "D");
        List<List<String>> iterative = allRoutesFinder.findAllRoutesIterative("A", "D");

        assertEquals(recursive.size(), iterative.size());
        assertTrue(iterative.containsAll(recursive));
        assertTrue(recursive.containsAll(iterative));
    }

    @Test
    void testIterativeHandlesCycles() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of("C"),
                        "C", List.of("A") // cycle
                )
        );

        List<List<String>> recursive = allRoutesFinder.findAllRoutes("A", "C");
        List<List<String>> iterative = allRoutesFinder.findAllRoutesIterative("A", "C");

        assertEquals(recursive, iterative);
    }

    @Test
    void testIterativeNoRoutes() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of(),
                        "C", List.of()
                )
        );

        assertTrue(allRoutesFinder.findAllRoutesIterative("A", "C").isEmpty());
    }

    @Test
    void testIterativeOriginEqualsDestination() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of()
                )
        );

        List<List<String>> routes = allRoutesFinder.findAllRoutesIterative("A", "A");

        assertEquals(1, routes.size());
        assertEquals(List.of("A"), routes.getFirst());
    }
}
