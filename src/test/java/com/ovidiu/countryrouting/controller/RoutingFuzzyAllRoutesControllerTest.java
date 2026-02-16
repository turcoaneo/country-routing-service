package com.ovidiu.countryrouting.controller;

import com.ovidiu.countryrouting.routing.RouteFinder;
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

@WebMvcTest(RoutingFuzzyAllRoutesController.class)
class RoutingFuzzyAllRoutesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    private RouteFinder routeFinder;

    // ---------------------------------------------------------
    // VALID ROUTE (FUZZY ALL ROUTES)
    // ---------------------------------------------------------
    @Test
    void testValidAllRoutesFuzzy() throws Exception {
        when(routeFinder.findAllRoutesFuzzy("SPN", "ITA", 5, 10))
                .thenReturn(List.of(
                        List.of("ESP", "FRA", "ITA")
                ));

        mockMvc.perform(get("/routing/fuzzy/all/SPN/ITA")
                        .param("maxDepth", "5")
                        .param("maxRoutes", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routes[0][0]").value("ESP"))
                .andExpect(jsonPath("$.routes[0][1]").value("FRA"))
                .andExpect(jsonPath("$.routes[0][2]").value("ITA"));
    }

    // ---------------------------------------------------------
    // INVALID FUZZY INPUT
    // ---------------------------------------------------------
    @Test
    void testInvalidFuzzyInput() throws Exception {
        when(routeFinder.findAllRoutesFuzzy("XXX", "ITA", 5, 10))
                .thenThrow(new IllegalArgumentException("Unknown or invalid country name/code"));

        mockMvc.perform(get("/routing/fuzzy/all/XXX/ITA")
                        .param("maxDepth", "5")
                        .param("maxRoutes", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown or invalid country name/code"));
    }

    // ---------------------------------------------------------
    // NO ROUTE FOUND
    // ---------------------------------------------------------
    @Test
    void testNoRouteFound() throws Exception {
        when(routeFinder.findAllRoutesFuzzy("ESP", "USA", 5, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/routing/fuzzy/all/ESP/USA")
                        .param("maxDepth", "5")
                        .param("maxRoutes", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routes").isArray())
                .andExpect(jsonPath("$.routes").isEmpty());
    }

    // ---------------------------------------------------------
    // DEFAULT MD/MR PARAMETERS
    // ---------------------------------------------------------
    @Test
    void testDefaultParameters() throws Exception {
        when(routeFinder.findAllRoutesFuzzy("SPN", "ITA", 10, 50))
                .thenReturn(List.of(
                        List.of("ESP", "FRA", "ITA")
                ));

        mockMvc.perform(get("/routing/fuzzy/all/SPN/ITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxDepth").value(10))
                .andExpect(jsonPath("$.maxRoutes").value(50))
                .andExpect(jsonPath("$.routes[0][0]").value("ESP"));
    }
}