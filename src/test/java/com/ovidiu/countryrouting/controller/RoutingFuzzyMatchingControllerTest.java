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

@WebMvcTest(RoutingFuzzyMatchingController.class)
class RoutingFuzzyMatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    private RouteFinder routeFinder;

    @Test
    void testValidFuzzyRoute() throws Exception {
        when(routeFinder.findShortestRouteFuzzy("SPN", "ITL"))
                .thenReturn(List.of("ESP", "FRA", "ITA"));

        mockMvc.perform(get("/routing/fuzzy/SPN/ITL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route[0]").value("ESP"))
                .andExpect(jsonPath("$.route[1]").value("FRA"))
                .andExpect(jsonPath("$.route[2]").value("ITA"));
    }

    @Test
    void testInvalidFuzzyCountry() throws Exception {
        when(routeFinder.findShortestRouteFuzzy("XXX", "ITA"))
                .thenThrow(new IllegalArgumentException("Unknown or invalid country name/code"));

        mockMvc.perform(get("/routing/fuzzy/XXX/ITA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown or invalid country name/code"));
    }

    @Test
    void testNoFuzzyRoute() throws Exception {
        when(routeFinder.findShortestRouteFuzzy("ESP", "USA"))
                .thenReturn(null);

        mockMvc.perform(get("/routing/fuzzy/ESP/USA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No land route found"));
    }
}
