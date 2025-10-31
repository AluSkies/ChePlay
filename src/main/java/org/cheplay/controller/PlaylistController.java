package org.cheplay.controller;

import org.cheplay.controller.playlist.PlaylistConstraintsFactory;
import org.cheplay.controller.playlist.PlaylistResponseMapper;
import org.cheplay.dto.PlaylistResponse;
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
        private final PlaylistConstraintsFactory constraintsFactory;
        private final PlaylistResponseMapper responseMapper;

        public PlaylistController(PlaylistGeneratorService playlistGeneratorService,
                                                          PlaylistConstraintsFactory constraintsFactory,
                                                          PlaylistResponseMapper responseMapper) {
                this.playlistGeneratorService = playlistGeneratorService;
                this.constraintsFactory = constraintsFactory;
                this.responseMapper = responseMapper;
    }

	@GetMapping(value = "/generate")
	public ResponseEntity<PlaylistResponse> generate(@RequestParam("user") String userId,
                                                                           @RequestParam(value = "size", defaultValue = "5") int size,
                                                                           @RequestParam(value = "uniqueArtist", defaultValue = "true") boolean uniqueArtist,
                                                                           @RequestParam(value = "minDuration", required = false) Integer minDuration,
                                                                           @RequestParam(value = "maxDuration", required = false) Integer maxDuration) {
                PlaylistConstraints constraints = constraintsFactory.create(size, uniqueArtist, minDuration, maxDuration);

                return playlistGeneratorService.generatePlaylist(userId, constraints)
                                .map(this::toResponse)
                                .map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());
    }

	private PlaylistResponse toResponse(GeneratedPlaylist playlist) {
                return responseMapper.toResponse(playlist);
    }
}
