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
    List<Map<String, Object>> direct = service.recommendDirectNeighbors(user, k);
    List<Map<String, Object>> shortest = service.recommendByShortestPath(user, k);
    Map<String, Object> closest = service.findClosestUser(user);

    // Use a mutable map to tolerate null values (closest may be null)
    Map<String, Object> out = new java.util.HashMap<>();
    out.put("user", user);
    out.put("directRecommendations", direct);
    out.put("shortestPathRecommendations", shortest);
        out.put("closest", closest);

        // Build a map neighbor -> shared songs (for the union of recommended neighbors)
        java.util.Set<String> neighbors = new java.util.HashSet<>();
        if (direct != null) {
            for (Map<String, Object> item : direct) {
                Object id = item.get("id");
                if (id instanceof String s) neighbors.add(s);
            }
        }
        if (shortest != null) {
            for (Map<String, Object> item : shortest) {
                Object id = item.get("id");
                if (id instanceof String s) neighbors.add(s);
            }
        }

        List<Map<String, Object>> neighborsWithSongs = service.getNeighborsWithSharedSongs(user);
        Map<String, List<Map<String, Object>>> sharedSongs = new java.util.HashMap<>();
        for (Map<String, Object> entry : neighborsWithSongs) {
            Object nb = entry.get("neighbor");
            if (nb == null) continue;
            String nbKey = nb.toString();
            if (neighbors.contains(nbKey)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> songs = (List<Map<String, Object>>) entry.get("songs");
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
    public List<Map<String, Object>> users() {
        return service.listUsersDecorated();
    }

    /**
     * Endpoint de depuración extendido: expone información de usuario, vecinos y grafo LIKED_SONG.
     */
    @GetMapping(value = "/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> debug(@RequestParam("user") String user,
                                     @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return service.debugUserData(user, limit);
    }

    @GetMapping(value = "/songs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String,Object>> recommendSongs(@RequestParam("user") String user,
                                                    @RequestParam(value = "k", defaultValue = "10") int k,
                                                    @RequestParam(value = "window", required = false) Integer window,
                                                    @RequestParam(value = "lambda", required = false) Double lambda) {
        // Use Prim-based recommendations exclusively (MST backbone + distances on the tree).
        return songService.recommendForUserUsingPrim(user, k, window, lambda);
    }

    @GetMapping(value = "/songs/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> debugSongs(@RequestParam("user") String user,
                                          @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return songService.debugListenedData(user, limit);
    }
}
