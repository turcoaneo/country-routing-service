package com.ovidiu.countryrouting.routing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AllRoutesCacheEntry {
    private int maxDepth;
    private int maxRoutes;
    private List<List<String>> routes;
}
