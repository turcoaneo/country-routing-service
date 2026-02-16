package com.ovidiu.countryrouting.controller;

import com.ovidiu.countryrouting.routing.RouteFinder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routing/fuzzy/all")
@Tag(name = "All Routes (Fuzzy)", description = "Find all land routes using fuzzy country matching with caching")
public class RoutingFuzzyAllRoutesController {

    private final RouteFinder routeFinder;

    @Autowired
    public RoutingFuzzyAllRoutesController(RouteFinder routeFinder) {
        this.routeFinder = routeFinder;
    }

    @GetMapping("/{origin}/{destination}")
    @Operation(summary = "Find all land routes (fuzzy)", description = "Uses fuzzy matching + caching + MD/MR filtering")
    public ResponseEntity<?> getAllRoutes(@PathVariable String origin,
                                          @PathVariable String destination,
                                          @RequestParam(defaultValue = "10") int maxDepth,
                                          @RequestParam(defaultValue = "50") int maxRoutes) {

        try {
            List<List<String>> routes =
                    routeFinder.findAllRoutesFuzzy(origin, destination, maxDepth, maxRoutes);

            return ResponseEntity.ok(Map.of(
                    "origin", origin,
                    "destination", destination,
                    "maxDepth", maxDepth,
                    "maxRoutes", maxRoutes,
                    "routes", routes
            ));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
