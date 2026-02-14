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

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "D", 10, 10);

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

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "C", 10, 10);
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

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "A", 10, 10);

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
                () -> allRoutesFinder.findAllRoutes("X", "B", 10, 10));
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

        List<List<String>> routes = allRoutesFinder.findAllRoutes("A", "C", 10, 10);

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

        List<List<String>> recursive = allRoutesFinder.findAllRoutes("A", "D", 10, 10);
        List<List<String>> iterative = allRoutesFinder.findAllRoutesIterative("A", "D", 10, 10);

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

        List<List<String>> recursive = allRoutesFinder.findAllRoutes("A", "C", 10, 10);
        List<List<String>> iterative = allRoutesFinder.findAllRoutesIterative("A", "C", 10, 10);

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

        assertTrue(allRoutesFinder.findAllRoutesIterative("A", "C", 10, 10).isEmpty());
    }

    @Test
    void testIterativeOriginEqualsDestination() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B"),
                        "B", List.of()
                )
        );

        List<List<String>> routes = allRoutesFinder.findAllRoutesIterative("A", "A", 10, 10);

        assertEquals(1, routes.size());
        assertEquals(List.of("A"), routes.getFirst());
    }

    @Test
    void benchmarkRecursiveVsIterative() {
        when(graphBuilder.buildGraph()).thenReturn(
                Map.of(
                        "A", List.of("B", "C"),
                        "B", List.of("D", "E"),
                        "C", List.of("F"),
                        "D", List.of("G"),
                        "E", List.of("G"),
                        "F", List.of("G"),
                        "G", List.of()
                )
        );

        int iterations = 10_000;
        long start, end;

        // Warm-up
        for (int i = 0; i < 1000; i++) {
            allRoutesFinder.findAllRoutes("A", "G", 10, 1000);
            allRoutesFinder.findAllRoutesIterative("A", "G", 10, 1000);
        }

        // Recursive benchmark
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            allRoutesFinder.findAllRoutes("A", "G", 10, 1000);
        }
        end = System.nanoTime();
        long recursiveTime = end - start;

        // Iterative benchmark
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            allRoutesFinder.findAllRoutesIterative("A", "G", 10, 1000);
        }
        end = System.nanoTime();
        long iterativeTime = end - start;

        System.out.println("Recursive DFS: " + recursiveTime / 1_000_000 + " ms");
        System.out.println("Iterative DFS: " + iterativeTime / 1_000_000 + " ms");
    }
}
