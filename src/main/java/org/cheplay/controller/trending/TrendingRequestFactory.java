package org.cheplay.controller.trending;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Builds {@link TrendingRequest} instances from raw controller inputs.
 */
@Component
public class TrendingRequestFactory {

    private final PlatformsParser platformsParser;

    public TrendingRequestFactory(PlatformsParser platformsParser) {
        this.platformsParser = platformsParser;
    }

    public TrendingRequest create(int k, List<String> rawPlatforms) {
        return new TrendingRequest(k, platformsParser.parse(rawPlatforms));
    }
}
