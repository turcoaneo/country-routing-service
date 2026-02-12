package com.ovidiu.countryrouting.graph;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphBuilderTest {

    @Test
    void testGraphIsBuiltCorrectly() {
        GraphBuilder builder = new GraphBuilder();
        Map<String, List<String>> graph = builder.buildGraph();

        assertFalse(graph.isEmpty());
        assertTrue(graph.containsKey("CZE"));
        assertTrue(graph.containsKey("ITA"));

        assertTrue(graph.get("CZE").contains("AUT"));
        assertTrue(graph.get("ITA").contains("AUT"));
    }
}
