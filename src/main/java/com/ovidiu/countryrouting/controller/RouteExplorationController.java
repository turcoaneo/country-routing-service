package com.ovidiu.countryrouting.controller;

import com.ovidiu.countryrouting.routing.AllRoutesFinder;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routing/all")
@Tag(name = "Route Exploration", description = "Find all possible land routes")
public class RouteExplorationController {

    private final AllRoutesFinder allRoutesFinder;

    public RouteExplorationController(AllRoutesFinder allRoutesFinder) {
        this.allRoutesFinder = allRoutesFinder;
    }

    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<?> getAllRoutes(
            @PathVariable String origin,
            @PathVariable String destination,
            @RequestParam(defaultValue = "10") int maxDepth,
            @RequestParam(defaultValue = "10") int maxRoutes
    ) {
        try {
            List<List<String>> routes = allRoutesFinder.findAllRoutes(
                    origin, destination, maxDepth, maxRoutes
            );

            if (routes.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No land routes found"));
            }

            return ResponseEntity.ok(Map.of("routes", routes));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/iterative/{origin}/{destination}")
    public ResponseEntity<?> getAllRoutesIteratively(
            @PathVariable String origin,
            @PathVariable String destination,
            @RequestParam(defaultValue = "15") int maxDepth,
            @RequestParam(defaultValue = "10") int maxRoutes
    ) {
        try {
            List<List<String>> routes = allRoutesFinder.findAllRoutesIterative(
                    origin, destination, maxDepth, maxRoutes
            );

            if (routes.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No land routes found"));
            }

            return ResponseEntity.ok(Map.of("routes", routes));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
