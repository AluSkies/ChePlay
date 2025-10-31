package org.cheplay.model.playlist;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the outcome of the playlist generator.
 */
public class GeneratedPlaylist {
    private final List<PlaylistSong> songs;
    private final int totalDuration;

    public GeneratedPlaylist(List<PlaylistSong> songs) {
        this.songs = List.copyOf(Objects.requireNonNull(songs, "songs"));
        this.totalDuration = songs.stream()
                .map(PlaylistSong::getDurationSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public List<PlaylistSong> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public int size() {
        return songs.size();
    }
}
