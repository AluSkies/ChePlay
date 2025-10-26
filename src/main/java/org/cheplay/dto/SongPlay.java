package org.cheplay.dto;

public class SongPlay {
    public final String songId;
    public final String title;
    public final String artist;
    public final int plays;

    public SongPlay(String songId, String title, String artist, int plays) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.plays = plays;
    }
}
