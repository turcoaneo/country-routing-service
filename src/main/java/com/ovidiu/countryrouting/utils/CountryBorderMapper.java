package com.ovidiu.countryrouting.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryBorderMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryRaw {
        private String cca2;
        private String ccn3;
        private String cca3;
        private String cioc;
        private List<String> borders;
    }

    @Data
    public static class CountryCompact {
        private String cca2;
        private String ccn3;
        private String cca3;
        private String cioc;
        private List<String> borders;
    }

    public Map<String, CountryCompact> loadAndTransform() throws IOException {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("data/countries.json");

        if (is == null) {
            throw new IllegalStateException("countries.json not found in resources/data/");
        }

        List<CountryRaw> rawList = MAPPER.readValue(is, new TypeReference<>() {
        });

        Map<String, CountryCompact> result = new HashMap<>();

        for (CountryRaw raw : rawList) {
            CountryCompact compact = new CountryCompact();
            compact.setCca2(raw.getCca2());
            compact.setCcn3(raw.getCcn3());
            compact.setCca3(raw.getCca3());
            compact.setCioc(raw.getCioc());
            compact.setBorders(raw.getBorders());

            result.put(raw.getCca3(), compact);
        }

        return result;
    }
}