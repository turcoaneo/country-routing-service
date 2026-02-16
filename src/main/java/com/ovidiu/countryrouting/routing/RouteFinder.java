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

    private final Map<String, List<String>> shortestRouteCache = new ConcurrentHashMap<>();

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
    // STRICT SHORTEST ROUTE
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
    // FUZZY SHORTEST ROUTE
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
    // ALL ROUTES WITH MD / MR + CACHING + REVERSE LOOKUP
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

        String keyForward = resolvedOrigin + "->" + resolvedDestination;
        String keyReverse = resolvedDestination + "->" + resolvedOrigin;

        AllRoutesCacheEntry cachedForward = allRoutesCache.getIfPresent(keyForward);
        // 1) Forward cache hit
        if (isCachedForward(maxDepth, maxRoutes, cachedForward))
            return filterRoutes(cachedForward.getRoutes(), maxDepth, maxRoutes);

        AllRoutesCacheEntry cachedReverse = allRoutesCache.getIfPresent(keyReverse);
        // 2) Reverse cache hit → reverse routes
        List<List<String>> reversed = getCachedReversed(maxDepth, maxRoutes, cachedReverse, keyForward);
        if (reversed != null) return reversed;

        // 3) No usable cache → compute
        Map<String, List<String>> graph = this.graphBuilder.buildGraph();

        if (!graph.containsKey(resolvedOrigin) || !graph.containsKey(resolvedDestination)) {
            throw new IllegalArgumentException("Resolved country not found in graph");
        }

        List<List<String>> allRoutes =
                computeAllRoutes(resolvedOrigin, resolvedDestination, maxDepth, maxRoutes, graph);

        AllRoutesCacheEntry newEntry = new AllRoutesCacheEntry(maxDepth, maxRoutes, allRoutes);
        allRoutesCache.put(keyForward, newEntry);
        persistence.save(allRoutesCache.asMap());

        return allRoutes;
    }

    private List<List<String>> getCachedReversed(int maxDepth, int maxRoutes, AllRoutesCacheEntry cachedReverse, String keyForward) {
        if (cachedReverse != null &&
                maxDepth <= cachedReverse.getMaxDepth() &&
                maxRoutes <= cachedReverse.getMaxRoutes()) {

            List<List<String>> reversed =
                    cachedReverse.getRoutes().stream()
                            .map(route -> {
                                List<String> copy = new ArrayList<>(route);
                                Collections.reverse(copy);
                                return copy;
                            })
                            .filter(route -> route.size() - 1 <= maxDepth)
                            .limit(maxRoutes)
                            .toList();

            // Store reversed result under forward key
            allRoutesCache.put(keyForward,
                    new AllRoutesCacheEntry(
                            cachedReverse.getMaxDepth(),
                            cachedReverse.getMaxRoutes(),
                            reversed
                    ));

            return reversed;
        }
        return null;
    }

    private boolean isCachedForward(int maxDepth, int maxRoutes, AllRoutesCacheEntry cachedForward) {
        return cachedForward != null &&
                maxDepth <= cachedForward.getMaxDepth() &&
                maxRoutes <= cachedForward.getMaxRoutes();
    }

    private List<List<String>> filterRoutes(List<List<String>> routes, int maxDepth, int maxRoutes) {
        return routes.stream()
                .filter(route -> route.size() - 1 <= maxDepth)
                .limit(maxRoutes)
                .toList();
    }

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
