package com.ovidiu.countryrouting.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CountryBorderMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryRaw {

        private Name name;

        private String cca2;
        private String ccn3;
        private String cca3;
        private String cioc;
        private List<String> borders;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Name {

            private String common;
            private String official;

            // Support both "native" and "nativeName"
            @JsonProperty("native")
            private Map<String, NativeName> nativeMap;

            @JsonProperty("nativeName")
            private Map<String, NativeName> nativeNameMap;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class NativeName {
                private String official;
                private String common;
            }

            public Map<String, NativeName> getAllNativeNames() {
                if (nativeMap != null) return nativeMap;
                if (nativeNameMap != null) return nativeNameMap;
                return Collections.emptyMap();
            }
        }
    }

    @Data
    public static class CountryCompact {

        private String cca2;
        private String ccn3;
        private String cca3;
        private String cioc;
        private List<String> borders;

        private List<String> names;
    }

    public Map<String, CountryCompact> loadAndTransform() throws IOException {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("data/countries.json");

        if (is == null) {
            throw new IllegalStateException("countries.json not found in resources/data/");
        }

        List<CountryRaw> rawList = MAPPER.readValue(is, new TypeReference<>() {});

        Map<String, CountryCompact> result = new HashMap<>();

        for (CountryRaw raw : rawList) {
            CountryCompact compact = new CountryCompact();
            compact.setCca2(raw.getCca2());
            compact.setCcn3(raw.getCcn3());
            compact.setCca3(raw.getCca3());
            compact.setCioc(raw.getCioc());
            compact.setBorders(raw.getBorders());

            // NEW: collect names
            List<String> names = new java.util.ArrayList<>();

            if (raw.getName() != null) {

                if (raw.getName().getCommon() != null)
                    names.add(raw.getName().getCommon());

                if (raw.getName().getOfficial() != null)
                    names.add(raw.getName().getOfficial());

                // NEW: unified native name extraction
                raw.getName().getAllNativeNames().values().forEach(n -> {
                    if (n.getCommon() != null) names.add(n.getCommon());
                    if (n.getOfficial() != null) names.add(n.getOfficial());
                });
            }

            compact.setNames(names);

            result.put(raw.getCca3(), compact);
        }

        return result;
    }
}