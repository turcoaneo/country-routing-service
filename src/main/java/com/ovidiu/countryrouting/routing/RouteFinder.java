package com.ovidiu.countryrouting.routing;

import com.ovidiu.countryrouting.fuzzymatching.CountryCodeResolver;
import com.ovidiu.countryrouting.graph.GraphBuilder;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@NoArgsConstructor
public class RouteFinder {

    private GraphBuilder graphBuilder;
    private CountryCodeResolver resolver;

    @Autowired
    public RouteFinder(GraphBuilder graphBuilder, CountryCodeResolver resolver) {
        this.graphBuilder = graphBuilder;
        this.resolver = resolver;
    }

    // ------------------------------------------------------------
    // ORIGINAL STRICT METHOD (kept for RoutingController)
    // ------------------------------------------------------------
    public List<String> findShortestRoute(String origin, String destination) {
        Map<String, List<String>> graph = this.graphBuilder.buildGraph();

        if (!graph.containsKey(origin) || !graph.containsKey(destination)) {
            throw new IllegalArgumentException("Unknown country code");
        }

        return bfs(origin, destination, graph);
    }

    // ------------------------------------------------------------
    // NEW FUZZY‑AWARE METHOD (for RoutingFuzzyMatchingController)
    // ------------------------------------------------------------
    public List<String> findShortestRouteFuzzy(String origin, String destination) {
        Map<String, List<String>> graph = this.graphBuilder.buildGraph();

        // Resolve fuzzy inputs

        try {
            resolver.extractData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String resolvedOrigin = resolver.resolve(origin);
        String resolvedDestination = resolver.resolve(destination);

        if (resolvedOrigin == null || resolvedDestination == null) {
            throw new IllegalArgumentException("Unknown or invalid country name/code");
        }

        if (!graph.containsKey(resolvedOrigin) || !graph.containsKey(resolvedDestination)) {
            throw new IllegalArgumentException("Resolved country not found in graph");
        }

        return bfs(resolvedOrigin, resolvedDestination, graph);
    }

    // ------------------------------------------------------------
    // BFS SHARED BY BOTH METHODS
    // ------------------------------------------------------------
    private List<String> bfs(String origin, String destination, Map<String, List<String>> graph) {
        if (origin.equals(destination)) {
            return List.of(origin);
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (String neighbor : graph.getOrDefault(current, List.of())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);

                    if (neighbor.equals(destination)) {
                        return reconstructPath(parent, origin, destination);
                    }
                }
            }
        }

        return null; // no land route
    }

    private List<String> reconstructPath(Map<String, String> parent,
                                         String origin,
                                         String destination) {

        List<String> path = new LinkedList<>();
        String step = destination;

        while (step != null) {
            path.addFirst(step);
            step = parent.get(step);
        }

        if (!path.getFirst().equals(origin)) {
            return null;
        }

        return path;
    }
}
