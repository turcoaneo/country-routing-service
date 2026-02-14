package com.ovidiu.countryrouting.controller;

import com.ovidiu.countryrouting.routing.AllRoutesFinder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteExplorationController.class)
class RouteExplorationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    private AllRoutesFinder allRoutesFinder;

    @Test
    void testMultipleRoutes() throws Exception {
        when(allRoutesFinder.findAllRoutes("A", "D", 10, 10))
                .thenReturn(List.of(
                        List.of("A", "B", "D"),
                        List.of("A", "C", "D")
                ));

        mockMvc.perform(get("/routing/all/A/D?maxDepth=10&maxRoutes=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routes[0][0]").value("A"))
                .andExpect(jsonPath("$.routes[0][1]").value("B"))
                .andExpect(jsonPath("$.routes[0][2]").value("D"))
                .andExpect(jsonPath("$.routes[1][0]").value("A"))
                .andExpect(jsonPath("$.routes[1][1]").value("C"))
                .andExpect(jsonPath("$.routes[1][2]").value("D"));
    }

    @Test
    void testNoRoutes() throws Exception {
        when(allRoutesFinder.findAllRoutes("USA", "AUS", 10, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/routing/all/USA/AUS?maxDepth=10&maxRoutes=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No land routes found"));
    }

    @Test
    void testInvalidCountry() throws Exception {
        when(allRoutesFinder.findAllRoutes("XXX", "ITA", 10, 10))
                .thenThrow(new IllegalArgumentException("Unknown country code"));

        mockMvc.perform(get("/routing/all/XXX/ITA?maxDepth=10&maxRoutes=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown country code"));
    }

    @Test
    void testMultipleRoutesIterative() throws Exception {
        when(allRoutesFinder.findAllRoutesIterative("A", "D", 10, 10))
                .thenReturn(List.of(
                        List.of("A", "B", "D"),
                        List.of("A", "C", "D")
                ));

        mockMvc.perform(get("/routing/all/iterative/A/D?maxDepth=10&maxRoutes=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routes[0][0]").value("A"))
                .andExpect(jsonPath("$.routes[0][1]").value("B"))
                .andExpect(jsonPath("$.routes[0][2]").value("D"))
                .andExpect(jsonPath("$.routes[1][0]").value("A"))
                .andExpect(jsonPath("$.routes[1][1]").value("C"))
                .andExpect(jsonPath("$.routes[1][2]").value("D"));
    }

    @Test
    void testNoRoutesIterative() throws Exception {
        when(allRoutesFinder.findAllRoutesIterative("USA", "AUS", 10, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/routing/all/iterative/USA/AUS?maxDepth=10&maxRoutes=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No land routes found"));
    }

    @Test
    void testInvalidCountryIterative() throws Exception {
        when(allRoutesFinder.findAllRoutesIterative("XXX", "ITA", 10, 10))
                .thenThrow(new IllegalArgumentException("Unknown country code"));

        mockMvc.perform(get("/routing/all/iterative/XXX/ITA?maxDepth=10&maxRoutes=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown country code"));
    }
}
