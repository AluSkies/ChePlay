package org.cheplay.model.playlist;

import java.util.Objects;

/**
 * Immutable view of a song candidate used by the playlist generator.
 */
public class PlaylistSong {
    private final String id;
    private final String title;
    private final String artist;
    private final Integer durationSeconds;
    private final Integer popularity;

    public PlaylistSong(String id, String title, String artist, Integer durationSeconds, Integer popularity) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = title != null ? title : id;
        this.artist = artist != null ? artist : "Unknown";
        this.durationSeconds = durationSeconds;
        this.popularity = popularity;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public Integer getPopularity() {
        return popularity;
    }
}
