package org.cheplay.controller.playlist;

import org.cheplay.model.playlist.PlaylistConstraints;
import org.springframework.stereotype.Component;

/**
 * Encapsulates construction of {@link PlaylistConstraints} from raw request data.
 */
@Component
public class PlaylistConstraintsFactory {

    public PlaylistConstraints create(int size, boolean uniqueArtist, Integer minDuration, Integer maxDuration) {
        return PlaylistConstraints.builder(size)
                .uniqueArtist(uniqueArtist)
                .minTotalDuration(normalizeDuration(minDuration))
                .maxTotalDuration(normalizeDuration(maxDuration))
                .build();
    }

    private Integer normalizeDuration(Integer candidate) {
        if (candidate == null || candidate <= 0) {
            return null;
        }
        return candidate;
    }
}
