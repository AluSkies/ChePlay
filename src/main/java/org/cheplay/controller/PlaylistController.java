package org.cheplay.controller;

import java.util.Map;

import org.cheplay.model.playlist.GeneratedPlaylist;
import org.cheplay.model.playlist.PlaylistConstraints;
import org.cheplay.model.playlist.PlaylistGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

    private final PlaylistGeneratorService playlistGeneratorService;

    public PlaylistController(PlaylistGeneratorService playlistGeneratorService) {
        this.playlistGeneratorService = playlistGeneratorService;
    }

    @GetMapping(value = "/generate")
    public ResponseEntity<?> generate(@RequestParam("user") String userId,
                                       @RequestParam(value = "size", defaultValue = "5") int size,
                                       @RequestParam(value = "uniqueArtist", defaultValue = "true") boolean uniqueArtist,
                                       @RequestParam(value = "minDuration", required = false) Integer minDuration,
                                       @RequestParam(value = "maxDuration", required = false) Integer maxDuration) {
        PlaylistConstraints constraints = PlaylistConstraints.builder(size)
                .uniqueArtist(uniqueArtist)
                .minTotalDuration(minDuration)
                .maxTotalDuration(maxDuration)
                .build();

        return playlistGeneratorService.generatePlaylist(userId, constraints)
                .<ResponseEntity<?>>map(playlist -> ResponseEntity.ok(buildResponse(playlist)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String, Object> buildResponse(GeneratedPlaylist playlist) {
        return Map.of(
                "size", playlist.size(),
                "totalDuration", playlist.getTotalDuration(),
                "songs", playlist.getSongs().stream().map(song -> Map.of(
                        "id", song.getId(),
                        "title", song.getTitle(),
                        "artist", song.getArtist(),
                        "duration", song.getDurationSeconds(),
                        "popularity", song.getPopularity()
                )).toList()
        );
    }
}
