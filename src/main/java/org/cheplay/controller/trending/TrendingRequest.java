package org.cheplay.controller.trending;

import java.util.List;

/**
 * Value object holding the normalized parameters for global trending queries.
 */
public record TrendingRequest(int limit, List<String> platforms) {

    public TrendingRequest {
        platforms = platforms == null ? List.of() : List.copyOf(platforms);
    }
}
