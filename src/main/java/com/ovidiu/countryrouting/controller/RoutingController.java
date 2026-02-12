package com.ovidiu.countryrouting.controller;

import com.ovidiu.countryrouting.graph.GraphBuilder;
import com.ovidiu.countryrouting.routing.RouteFinder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routing")
public class RoutingController {

    private final RouteFinder routeFinder;

    public RoutingController() {
        Map<String, List<String>> graph = new GraphBuilder().buildGraph();
        this.routeFinder = new RouteFinder(graph);
    }

    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<?> getRoute(@PathVariable String origin,
                                      @PathVariable String destination) {

        try {
            List<String> route = routeFinder.findShortestRoute(origin, destination);

            if (route == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No land route found"));
            }

            return ResponseEntity.ok(Map.of("route", route));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
