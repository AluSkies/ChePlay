package org.cheplay.controller;

import java.util.List;
import java.util.Map;

import org.cheplay.dto.MovieWatch;
import org.cheplay.model.trending.MovieTrendingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for movie trending analytics using
 * MergeSort, Greedy, and BFS algorithms.
 */
@RestController
@RequestMapping("/api/movies/trending")
public class MovieTrendingController {

    private final MovieTrendingService service;

    public MovieTrendingController(MovieTrendingService service) {
        this.service = service;
    }

    /**
     * Get global trending movies using Greedy Top-K algorithm.
     * 
     * GET /api/movies/trending/global?k=10
     */
    @GetMapping(value = "/global", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MovieWatch> topGlobal(
        @RequestParam(value = "k", defaultValue = "10") int k
    ) {
        return service.topGlobalTrending(k);
    }

    /**
     * Get trending movies by genre using Greedy + MergeSort.
     * 
     * GET /api/movies/trending/by-genre?genre=Sci-Fi&k=10
     */
    @GetMapping(value = "/by-genre", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MovieWatch> trendingByGenre(
        @RequestParam("genre") String genre,
        @RequestParam(value = "k", defaultValue = "10") int k
    ) {
        return service.trendingMoviesByGenre(genre, k);
    }

    /**
     * Get influential movies that drive most subsequent views.
     * Uses BFS to track influence propagation.
     * 
     * GET /api/movies/trending/influential?k=10
     */
    @GetMapping(value = "/influential", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> trendingWithInfluence(
        @RequestParam(value = "k", defaultValue = "10") int k
    ) {
        return service.trendingWithInfluence(k);
    }

    /**
     * Compare user taste with global trending.
     * 
     * GET /api/movies/trending/compare?user=alice&k=10
     */
    @GetMapping(value = "/compare", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> compareUserTaste(
        @RequestParam("user") String user,
        @RequestParam(value = "k", defaultValue = "10") int k
    ) {
        return service.compareUserTaste(user, k);
    }
}

