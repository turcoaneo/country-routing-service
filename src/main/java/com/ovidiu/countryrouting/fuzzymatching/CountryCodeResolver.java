package com.ovidiu.countryrouting.fuzzymatching;

import com.ovidiu.countryrouting.utils.CountryBorderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class CountryCodeResolver {

    private final Map<String, String> exactCodeMap = new HashMap<>();
    private final Map<String, String> exactNameMap = new HashMap<>();
    private final List<String> allCca3Codes = new ArrayList<>();
    private final Map<String, List<String>> namesByCca3 = new HashMap<>();

    private final CountryBorderMapper mapper;

    @Autowired
    public CountryCodeResolver(CountryBorderMapper mapper) {
        this.mapper = mapper;
    }

    /** Load and index all data from borders.json */
    public void extractData() throws IOException {
        Map<String, CountryBorderMapper.CountryCompact> data = this.mapper.loadAndTransform();

        for (var entry : data.entrySet()) {
            String cca3 = entry.getKey();
            CountryBorderMapper.CountryCompact compact = entry.getValue();

            allCca3Codes.add(cca3);
            namesByCca3.put(cca3, compact.getNames());

            addExact(compact.getCca3(), cca3);
            addExact(compact.getCca2(), cca3);
            addExact(compact.getCcn3(), cca3);
            addExact(compact.getCioc(), cca3);

            for (String name : compact.getNames()) {
                exactNameMap.put(name.toUpperCase(), cca3);
            }
        }
    }

    private void addExact(String code, String cca3) {
        if (code != null && !code.isBlank()) {
            exactCodeMap.put(code.toUpperCase(), cca3);
        }
    }

    /** Main resolver entry point */
    public String resolve(String input) {
        if (input == null) return null;

        String normalized = input.trim().toUpperCase();

        // 0. Reject garbage early
        if (isGarbage(normalized)) return null;

        // 1. Exact code match
        if (exactCodeMap.containsKey(normalized)) {
            return exactCodeMap.get(normalized);
        }

        // 2. Exact name match
        if (exactNameMap.containsKey(normalized)) {
            return exactNameMap.get(normalized);
        }

        // 3. Fuzzy name match (strongest)
        String nameMatch = fuzzyMatchNames(normalized);
        if (nameMatch != null) return nameMatch;

        // 4. Fuzzy code match (fallback)
        return fuzzyMatchCodes(normalized);
    }

    // ---------------------------------------------------------
    // Garbage Detection
    // ---------------------------------------------------------
    private boolean isGarbage(String s) {
        // numeric only
        if (s.matches("\\d+")) return true;

        // no vowels and length > 3 → likely garbage
        if (!s.matches(".*[AEIOU].*") && s.length() > 3) return true;

        // too long to be a code and not a name prefix
        if (s.length() > 12) return true;

        // high-entropy consonant clusters (XYZ, QWZ, etc.)
        return s.matches("[BCDFGHJKLMNPQRSTVWXYZ]{3,}");
    }

    // ---------------------------------------------------------
    // Fuzzy Matching on Names (dominant)
    // ---------------------------------------------------------
    private String fuzzyMatchNames(String input) {
        int bestScore = Integer.MIN_VALUE;
        String bestMatch = null;

        for (var entry : namesByCca3.entrySet()) {
            String cca3 = entry.getKey();
            List<String> names = entry.getValue();

            for (String name : names) {
                int score = scoreNameMatch(input, name);

                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = cca3;
                }
            }
        }

        return bestScore >= 70 ? bestMatch : null;
    }

    private int scoreNameMatch(String input, String name) {
        String n = name.toUpperCase();

        int dist = levenshtein(input, n);
        int score = 120 - dist * 10;

        // prefix match is very strong
        if (n.startsWith(input)) score += 40;

        // substring match
        if (n.contains(input)) score += 20;

        // consonant/vowel pattern match
        if (pattern(input).equals(pattern(n))) score += 10;

        return score;
    }

    // ---------------------------------------------------------
    // Fuzzy Matching on Codes (fallback)
    // ---------------------------------------------------------
    private String fuzzyMatchCodes(String input) {
        int bestScore = Integer.MIN_VALUE;
        String bestMatch = null;

        for (String cca3 : allCca3Codes) {
            int score = scoreCodeMatch(input, cca3);

            if (score > bestScore) {
                bestScore = score;
                bestMatch = cca3;
            }
        }

        return bestScore >= 60 ? bestMatch : null;
    }

    private int scoreCodeMatch(String input, String cca3) {
        int dist = levenshtein(input, cca3);

        int score = 100 - dist * 25;

        if (cca3.startsWith(input.substring(0, 1))) score += 10;
        if (pattern(input).equals(pattern(cca3))) score += 10;

        return score;
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private String pattern(String s) {
        return s.replaceAll("[AEIOU]", "V").replaceAll("[^V]", "C");
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}
