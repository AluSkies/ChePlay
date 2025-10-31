package org.cheplay.controller.playlist;

import java.util.List;
import java.util.stream.Collectors;

import org.cheplay.dto.PlaylistResponse;
import org.cheplay.model.playlist.GeneratedPlaylist;
import org.cheplay.model.playlist.PlaylistSong;
import org.springframework.stereotype.Component;

/**
 * Translates domain playlists into serialized API responses.
 */
@Component
public class PlaylistResponseMapper {

    public PlaylistResponse toResponse(GeneratedPlaylist playlist) {
        List<PlaylistResponse.Song> songs = playlist.getSongs().stream()
                .map(this::toResponseSong)
                .collect(Collectors.toList());
        return new PlaylistResponse(playlist.size(), playlist.getTotalDuration(), songs);
    }

    private PlaylistResponse.Song toResponseSong(PlaylistSong song) {
        return new PlaylistResponse.Song(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getDurationSeconds(),
                song.getPopularity()
        );
    }
}
