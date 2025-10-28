package org.cheplay.controller;

import java.util.List;
import java.util.Map;

import org.cheplay.model.recommendation.FriendRecommendationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final FriendRecommendationService service;
    private final org.cheplay.model.recommendation.SongRecommendationService songService;

    public RecommendationController(FriendRecommendationService service,
                                    org.cheplay.model.recommendation.SongRecommendationService songService) {
        this.service = service;
        this.songService = songService;
    }

    /**
     * Devuelve recomendaciones y el usuario más cercano para el user dado.
     */
    @GetMapping(value = "/closest", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> closest(@RequestParam("user") String user,
                                       @RequestParam(value = "k", defaultValue = "10") int k) {
    List<String> direct = service.recommendDirectNeighbors(user, k);
    List<String> shortest = service.recommendByShortestPath(user, k);
    String closest = service.findClosestUser(user);

    // Use a mutable map to tolerate null values (closest may be null)
    Map<String, Object> out = new java.util.HashMap<>();
    out.put("user", user);
    out.put("directRecommendations", direct);
    out.put("shortestPathRecommendations", shortest);
        out.put("closest", closest);

        // Build a map neighbor -> shared songs (for the union of recommended neighbors)
        java.util.Set<String> neighbors = new java.util.HashSet<>();
        if (direct != null) neighbors.addAll(direct);
        if (shortest != null) neighbors.addAll(shortest);

        List<Map<String, Object>> neighborsWithSongs = service.getNeighborsWithSharedSongs(user);
        Map<String, List<String>> sharedSongs = new java.util.HashMap<>();
        for (Map<String, Object> entry : neighborsWithSongs) {
            Object nb = entry.get("neighbor");
            if (nb == null) continue;
            String nbKey = nb.toString();
            if (neighbors.contains(nbKey)) {
                @SuppressWarnings("unchecked")
                List<String> songs = (List<String>) entry.get("songs");
                sharedSongs.put(nbKey, songs);
            }
        }
        out.put("sharedSongs", sharedSongs);
    return out;
    }

    /**
     * Devuelve la lista de claves de usuario que el servicio usa como identificador.
     */
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> users() {
        return service.listUserKeys();
    }

    /**
     * Endpoint de depuración: devuelve la clave resuelta del usuario, las canciones que marcó LIKED_SONG,
     * y la lista de vecinos con overlap (número de canciones en común) y peso.
     */
    @GetMapping(value = "/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> debug(@RequestParam("user") String user) {
        String resolved = service.findUserKeyInDb(user);
        List<String> songs = service.getUserLikedSongs(user);
        List<Map<String, Object>> neighbors = service.getNeighborsWithOverlap(user);

        Map<String, Object> out = new java.util.HashMap<>();
        out.put("input", user);
        out.put("resolvedKey", resolved);
        out.put("likedSongs", songs);
        out.put("neighbors", neighbors);
        return out;
    }

    @GetMapping(value = "/songs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String,Object>> recommendSongs(@RequestParam("user") String user,
                                                    @RequestParam(value = "k", defaultValue = "10") int k,
                                                    @RequestParam(value = "window", required = false) Integer window,
                                                    @RequestParam(value = "lambda", required = false) Double lambda) {
        // Use Prim-based recommendations exclusively (MST backbone + distances on the tree).
        return songService.recommendForUserUsingPrim(user, k, window, lambda);
    }
}
