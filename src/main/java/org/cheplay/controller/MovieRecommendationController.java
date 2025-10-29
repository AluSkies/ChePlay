package org.cheplay.controller;

import java.util.List;
import java.util.Map;

import org.cheplay.model.recommendation.MovieRecommendationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for movie recommendations using graph algorithms.
 */
@RestController
@RequestMapping("/api/movies/recommendations")
public class MovieRecommendationController {

    private final MovieRecommendationService service;

    public MovieRecommendationController(MovieRecommendationService service) {
        this.service = service;
    }

    /**
     * Recommend movies using BFS algorithm.
     * Explores movies layer-by-layer from user's watched movies.
     * 
     * GET /api/movies/recommendations/bfs?user=alice&k=10&maxDepth=3
     */
    @GetMapping(value = "/bfs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> recommendBFS(
        @RequestParam("user") String user,
        @RequestParam(value = "k", defaultValue = "10") int k,
        @RequestParam(value = "maxDepth", required = false) Integer maxDepth
    ) {
        return service.recommendMoviesBFS(user, k, maxDepth);
    }

    /**
     * Recommend movies using DFS algorithm.
     * Deep exploration of similarity chains for niche discoveries.
     * 
     * GET /api/movies/recommendations/dfs?user=alice&k=10&maxDepth=4
     */
    @GetMapping(value = "/dfs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> recommendDFS(
        @RequestParam("user") String user,
        @RequestParam(value = "k", defaultValue = "10") int k,
        @RequestParam(value = "maxDepth", required = false) Integer maxDepth
    ) {
        return service.recommendMoviesDFS(user, k, maxDepth);
    }

    /**
     * Recommend diverse movies using Kruskal MST algorithm.
     * Ensures variety without redundancy.
     * 
     * GET /api/movies/recommendations/diverse?user=alice&k=10
     */
    @GetMapping(value = "/diverse", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> recommendDiverse(
        @RequestParam("user") String user,
        @RequestParam(value = "k", defaultValue = "10") int k
    ) {
        return service.recommendMoviesDiverse(user, k);
    }

    /**
     * Recommend movies by genre using BFS.
     * 
     * GET /api/movies/recommendations/by-genre?user=alice&genre=Sci-Fi&k=10
     */
    @GetMapping(value = "/by-genre", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> recommendByGenre(
        @RequestParam("user") String user,
        @RequestParam("genre") String genre,
        @RequestParam(value = "k", defaultValue = "10") int k
    ) {
        return service.recommendMoviesByGenre(user, genre, k);
    }

    /**
     * Recommend movies sorted by MergeSort algorithm.
     * 
     * GET /api/movies/recommendations/sorted?user=alice&k=10
     */
    @GetMapping(value = "/sorted", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> recommendSorted(
        @RequestParam("user") String user,
        @RequestParam(value = "k", defaultValue = "10") int k
    ) {
        return service.recommendMoviesSorted(user, k);
    }

    /**
     * Debug endpoint to inspect user's watched movies data.
     * 
     * GET /api/movies/recommendations/debug?user=alice&limit=20
     */
    @GetMapping(value = "/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> debug(
        @RequestParam("user") String user,
        @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        return service.debugWatchedData(user, limit);
    }
}

