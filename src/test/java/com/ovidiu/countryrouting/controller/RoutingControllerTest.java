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

@WebMvcTest(RoutingController.class)
class RoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    private RouteFinder routeFinder;

    @Test
    void testValidRoute() throws Exception {
        when(routeFinder.findShortestRoute("CZE", "ITA"))
                .thenReturn(List.of("CZE", "AUT", "ITA"));

        mockMvc.perform(get("/routing/CZE/ITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route[0]").value("CZE"))
                .andExpect(jsonPath("$.route[1]").value("AUT"))
                .andExpect(jsonPath("$.route[2]").value("ITA"));
    }

    @Test
    void testInvalidCountry() throws Exception {
        when(routeFinder.findShortestRoute("XXX", "ITA"))
                .thenThrow(new IllegalArgumentException("Unknown country code"));

        mockMvc.perform(get("/routing/XXX/ITA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown country code"));
    }

    @Test
    void testNoRoute() throws Exception {
        when(routeFinder.findShortestRoute("USA", "AUS"))
                .thenReturn(null);

        mockMvc.perform(get("/routing/USA/AUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No land route found"));
    }
}
