package com.ovidiu.countryrouting.graph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class GraphBuilderTest {
    @Spy
    GraphBuilder builder;

    @Test
    void testGraphIsBuiltCorrectly() {
        Map<String, List<String>> graph = builder.buildGraph();

        assertFalse(graph.isEmpty());
        assertTrue(graph.containsKey("CZE"));
        assertTrue(graph.containsKey("ITA"));

        assertTrue(graph.get("CZE").contains("AUT"));
        assertTrue(graph.get("ITA").contains("AUT"));
    }
}
