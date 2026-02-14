package com.ovidiu.countryrouting.routing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

@SpringBootTest
class DfsParameterizedBenchmarkTest {

    @Autowired
    private AllRoutesFinder finder;

    @ParameterizedTest
    @MethodSource("countryPairs")
    void benchmarkRecursiveVsIterative(String origin, String destination) {

        // Warm-up (JIT stabilization)
        finder.findAllRoutes(origin, destination, 10, 1000);
        finder.findAllRoutesIterative(origin, destination, 10, 1000);
    }

    static Stream<Arguments> countryPairs() {
        return Stream.of(
                Arguments.of("CZE", "ITA"),
                Arguments.of("ESP", "FRA"),
                Arguments.of("NOR", "GRC"),
                Arguments.of("PRT", "ROU"),
                Arguments.of("DEU", "TUR"),
                Arguments.of("FIN", "ESP"),
                Arguments.of("SWE", "ITA"),
                Arguments.of("POL", "GRC"),
                Arguments.of("AUT", "NLD"),
                Arguments.of("BEL", "ROU")
        );
    }
}
