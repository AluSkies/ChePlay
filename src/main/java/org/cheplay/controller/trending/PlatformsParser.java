package org.cheplay.controller.trending;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Normalizes the optional list of platform filters coming from HTTP requests.
 */
@Component
public class PlatformsParser {

    public List<String> parse(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        Set<String> ordered = new LinkedHashSet<>();
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            for (String token : entry.split(",")) {
                String candidate = token.trim();
                if (!candidate.isEmpty()) {
                    ordered.add(candidate);
                }
            }
        }
        return List.copyOf(ordered);
    }
}
