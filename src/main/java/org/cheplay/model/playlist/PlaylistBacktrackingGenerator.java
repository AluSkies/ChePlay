package org.cheplay.model.playlist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Stateless helper that runs a simple backtracking strategy to build a playlist that satisfies the provided constraints.
 */
class PlaylistBacktrackingGenerator {

    GeneratedPlaylist generate(List<PlaylistSong> candidates, PlaylistConstraints constraints) {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(constraints, "constraints");

        List<PlaylistSong> ordered = new ArrayList<>(candidates);
        // Simple heuristic: try more popular songs first when available.
        ordered.sort((a, b) -> {
            Integer pa = a.getPopularity();
            Integer pb = b.getPopularity();
            if (pa == null && pb == null) return 0;
            if (pa == null) return 1;
            if (pb == null) return -1;
            return Integer.compare(pb, pa);
        });

        List<PlaylistSong> current = new ArrayList<>();
        Set<String> usedArtists = new HashSet<>();
        Set<String> usedSongIds = new HashSet<>();
        List<PlaylistSong> best = new ArrayList<>();

        backtrack(ordered, constraints, 0, 0, current, usedArtists, usedSongIds, best);
        return best.isEmpty() ? null : new GeneratedPlaylist(best);
    }

    private void backtrack(List<PlaylistSong> ordered,
                           PlaylistConstraints constraints,
                           int index,
                           int currentDuration,
                           List<PlaylistSong> current,
                           Set<String> usedArtists,
                           Set<String> usedSongIds,
                           List<PlaylistSong> best) {
        if (current.size() == constraints.getTargetSize()) {
            Integer minDuration = constraints.getMinTotalDuration();
            if (minDuration != null && currentDuration < minDuration) {
                return;
            }
            // First valid playlist wins because of ordering (higher popularity first).
            if (best.isEmpty()) {
                best.addAll(current);
            }
            return;
        }

        if (index >= ordered.size()) {
            return;
        }

        for (int i = index; i < ordered.size(); i++) {
            PlaylistSong song = ordered.get(i);
            if (usedSongIds.contains(song.getId())) {
                continue;
            }
            if (constraints.isUniqueArtist() && usedArtists.contains(song.getArtist())) {
                continue;
            }

            Integer duration = song.getDurationSeconds();
            int newDuration = currentDuration + (duration != null ? duration : 0);
            Integer maxDuration = constraints.getMaxTotalDuration();
            if (maxDuration != null && newDuration > maxDuration) {
                continue;
            }

            current.add(song);
            usedSongIds.add(song.getId());
            if (constraints.isUniqueArtist()) {
                usedArtists.add(song.getArtist());
            }

            backtrack(ordered, constraints, i + 1, newDuration, current, usedArtists, usedSongIds, best);

            current.remove(current.size() - 1);
            usedSongIds.remove(song.getId());
            if (constraints.isUniqueArtist()) {
                usedArtists.remove(song.getArtist());
            }

            if (!best.isEmpty()) {
                return; // stop exploring once we have a valid playlist.
            }
        }
    }
}
