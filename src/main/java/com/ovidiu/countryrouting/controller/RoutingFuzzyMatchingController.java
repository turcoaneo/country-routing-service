package com.ovidiu.countryrouting.controller;

import com.ovidiu.countryrouting.routing.RouteFinder;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routing/fuzzy")
@Tag(name = "Shortest Route (Fuzzy)", description = "Find the shortest land route using fuzzy country matching")
public class RoutingFuzzyMatchingController {

    private final RouteFinder routeFinder;

    @Autowired
    public RoutingFuzzyMatchingController(RouteFinder routeFinder) {
        this.routeFinder = routeFinder;
    }

    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<?> getRoute(@PathVariable String origin,
                                      @PathVariable String destination) {

        try {
            List<String> route = routeFinder.findShortestRouteFuzzy(origin, destination);

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
