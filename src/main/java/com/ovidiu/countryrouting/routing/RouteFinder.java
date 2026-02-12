package com.ovidiu.countryrouting.routing;


import java.util.*;

public class RouteFinder {

    private final Map<String, List<String>> graph;

    public RouteFinder(Map<String, List<String>> graph) {
        this.graph = graph;
    }

    public List<String> findShortestRoute(String origin, String destination) {
        if (!graph.containsKey(origin) || !graph.containsKey(destination)) {
            throw new IllegalArgumentException("Unknown country code");
        }

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
