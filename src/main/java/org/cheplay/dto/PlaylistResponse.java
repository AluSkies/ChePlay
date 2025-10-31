package org.cheplay.dto;

import java.util.List;

/**
 * API payload for playlist generation responses.
 */
public class PlaylistResponse {

    private final int size;
    private final Integer totalDuration;
    private final List<Song> songs;

    public PlaylistResponse(int size, Integer totalDuration, List<Song> songs) {
        this.size = size;
        this.totalDuration = totalDuration;
        this.songs = songs == null ? List.of() : List.copyOf(songs);
    }

    public int getSize() {
        return size;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public static final class Song {

        private final String id;
        private final String title;
        private final String artist;
        private final Integer duration;
        private final Integer popularity;

        public Song(String id, String title, String artist, Integer duration, Integer popularity) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.duration = duration;
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

        public Integer getDuration() {
            return duration;
        }

        public Integer getPopularity() {
            return popularity;
        }
    }
}
