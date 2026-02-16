package com.ovidiu.countryrouting.routing;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ovidiu.countryrouting.fuzzymatching.CountryCodeResolver;
import com.ovidiu.countryrouting.graph.GraphBuilder;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@NoArgsConstructor
public class RouteFinder {

    private GraphBuilder graphBuilder;
    private CountryCodeResolver resolver;

    // shortest-route cache (optional, simple)
    private final Map<String, List<String>> shortestRouteCache = new ConcurrentHashMap<>();

    // all-routes cache (Caffeine + persistence)
    private final Cache<String, AllRoutesCacheEntry> allRoutesCache =
            Caffeine.newBuilder()
                    .maximumSize(5000)
                    .build();

    private final AllRoutesCachePersistence persistence = new AllRoutesCachePersistence();

    @Autowired
    public RouteFinder(GraphBuilder graphBuilder, CountryCodeResolver resolver) {
        this.graphBuilder = graphBuilder;
        this.resolver = resolver;
        try {
            resolver.extractData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void loadCache() {
        Map<String, AllRoutesCacheEntry> loaded = persistence.load();
        allRoutesCache.putAll(loaded);

    }

    @PreDestroy
    public void saveCache() {
        persistence.save(allRoutesCache.asMap());
    }

    // ------------------------------------------------------------
    // STRICT SHORTEST ROUTE (existing)
    // ------------------------------------------------------------
    public List<String> findShortestRoute(String origin, String destination) {
        String key = origin + "->" + destination;

        return shortestRouteCache.computeIfAbsent(key, k -> {
            Map<String, List<String>> graph = this.graphBuilder.buildGraph();

            if (!graph.containsKey(origin) || !graph.containsKey(destination)) {
                throw new IllegalArgumentException("Unknown country code");
            }

            return bfs(origin, destination, graph);
        });
    }

    // ------------------------------------------------------------
    // FUZZY SHORTEST ROUTE (existing)
    // ------------------------------------------------------------
    public List<String> findShortestRouteFuzzy(String origin, String destination) {
        Map<String, List<String>> graph = this.graphBuilder.buildGraph();

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
    // ALL ROUTES WITH MD / MR + CACHING + FILTERING
    // ------------------------------------------------------------
    public List<List<String>> findAllRoutesFuzzy(String origin,
                                                 String destination,
                                                 int maxDepth,
                                                 int maxRoutes) {

        String resolvedOrigin = resolver.resolve(origin);
        String resolvedDestination = resolver.resolve(destination);

        if (resolvedOrigin == null || resolvedDestination == null) {
            throw new IllegalArgumentException("Unknown or invalid country name/code");
        }

        String key = resolvedOrigin + "->" + resolvedDestination;

        AllRoutesCacheEntry cached = allRoutesCache.getIfPresent(key);

        if (cached != null &&
                maxDepth <= cached.getMaxDepth() &&
                maxRoutes <= cached.getMaxRoutes()) {

            // cache hit with sufficient MD/MR → filter only, no graphBuilder call
            return filterRoutes(cached.getRoutes(), maxDepth, maxRoutes);
        }

        // only now do we need the graph
        Map<String, List<String>> graph = this.graphBuilder.buildGraph();

        if (!graph.containsKey(resolvedOrigin) || !graph.containsKey(resolvedDestination)) {
            throw new IllegalArgumentException("Resolved country not found in graph");
        }

        List<List<String>> allRoutes =
                computeAllRoutes(resolvedOrigin, resolvedDestination, maxDepth, maxRoutes, graph);

        AllRoutesCacheEntry newEntry = new AllRoutesCacheEntry(maxDepth, maxRoutes, allRoutes);
        allRoutesCache.put(key, newEntry);
        persistence.save(allRoutesCache.asMap());

        return allRoutes;
    }

    private List<List<String>> filterRoutes(List<List<String>> routes, int maxDepth, int maxRoutes) {
        return routes.stream()
                .filter(route -> route.size() - 1 <= maxDepth)
                .limit(maxRoutes)
                .toList();
    }

    // Simple DFS with depth and route limits
    private List<List<String>> computeAllRoutes(String origin,
                                                String destination,
                                                int maxDepth,
                                                int maxRoutes,
                                                Map<String, List<String>> graph) {

        List<List<String>> result = new ArrayList<>();
        Deque<String> path = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        path.addLast(origin);
        visited.add(origin);

        dfsAll(origin, destination, maxDepth, maxRoutes, graph, path, visited, result);

        return result;
    }

    private void dfsAll(String current,
                        String destination,
                        int maxDepth,
                        int maxRoutes,
                        Map<String, List<String>> graph,
                        Deque<String> path,
                        Set<String> visited,
                        List<List<String>> result) {

        if (result.size() >= maxRoutes) {
            return;
        }

        if (current.equals(destination)) {
            result.add(new ArrayList<>(path));
            return;
        }

        if (path.size() - 1 >= maxDepth) {
            return;
        }

        for (String neighbor : graph.getOrDefault(current, List.of())) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                path.addLast(neighbor);

                dfsAll(neighbor, destination, maxDepth, maxRoutes, graph, path, visited, result);

                path.removeLast();
                visited.remove(neighbor);

                if (result.size() >= maxRoutes) {
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------
    // BFS (existing shortest path)
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

        return null;
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
