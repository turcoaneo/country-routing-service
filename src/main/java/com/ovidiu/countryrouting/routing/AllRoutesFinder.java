package com.ovidiu.countryrouting.routing;

import com.ovidiu.countryrouting.aop.TrackExecutionTime;
import com.ovidiu.countryrouting.graph.GraphBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AllRoutesFinder {

    private final GraphBuilder graphBuilder;

    public AllRoutesFinder(GraphBuilder graphBuilder) {
        this.graphBuilder = graphBuilder;
    }

    @TrackExecutionTime(includeArgs = true)
    public List<List<String>> findAllRoutes(
            String origin,
            String destination,
            int maxDepth,
            int maxRoutes
    ) {
        Map<String, List<String>> graph = graphBuilder.buildGraph();

        if (!graph.containsKey(origin) || !graph.containsKey(destination)) {
            throw new IllegalArgumentException("Unknown country code");
        }

        List<List<String>> results = new ArrayList<>();
        LinkedList<String> path = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        dfs(graph, origin, destination, visited, path, results, maxDepth, maxRoutes);

        return results;
    }

    private void dfs(
            Map<String, List<String>> graph,
            String current,
            String destination,
            Set<String> visited,
            LinkedList<String> path,
            List<List<String>> results,
            int maxDepth,
            int maxRoutes
    ) {
        if (path.size() > maxDepth || results.size() >= maxRoutes) {
            return;
        }

        visited.add(current);
        path.add(current);

        if (current.equals(destination)) {
            results.add(new ArrayList<>(path));
        } else {
            for (String neighbor : graph.getOrDefault(current, List.of())) {
                if (!visited.contains(neighbor)) {
                    dfs(graph, neighbor, destination, visited, path, results, maxDepth, maxRoutes);
                }
            }
        }

        path.removeLast();
        visited.remove(current);
    }

    @TrackExecutionTime(includeArgs = true)
    public List<List<String>> findAllRoutesIterative(
            String origin,
            String destination,
            int maxDepth,
            int maxRoutes
    ) {
        Map<String, List<String>> graph = getGraph(origin, destination);

        List<List<String>> results = new ArrayList<>();

        // Stack holds (currentNode, currentPath, visitedSet)
        Deque<NodeState> stack = new ArrayDeque<>();

        stack.push(new NodeState(
                origin,
                List.of(origin),
                Set.of(origin)
        ));

        while (!stack.isEmpty() && results.size() < maxRoutes) {
            NodeState state = stack.pop();
            String current = state.node();
            List<String> path = state.path();
            Set<String> visited = state.visited();

            if (current.equals(destination)) {
                results.add(path);
                continue;
            }

            if (path.size() >= maxDepth) {
                continue;
            }

            for (String neighbor : graph.getOrDefault(current, List.of())) {
                if (!visited.contains(neighbor)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);

                    Set<String> newVisited = new HashSet<>(visited);
                    newVisited.add(neighbor);

                    stack.push(new NodeState(neighbor, newPath, newVisited));
                }
            }
        }

        return results;
    }

    private record NodeState(String node, List<String> path, Set<String> visited) {
    }

    private Map<String, List<String>> getGraph(String origin, String destination) {
        Map<String, List<String>> graph = graphBuilder.buildGraph();

        if (!graph.containsKey(origin) || !graph.containsKey(destination)) {
            throw new IllegalArgumentException("Unknown country code");
        }
        return graph;
    }
}