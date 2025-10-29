package org.cheplay.controller;

import java.util.List;
import java.util.Map;

import org.cheplay.dto.MarathonPlan;
import org.cheplay.dto.MarathonRequest;
import org.cheplay.model.marathon.MovieMarathonService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for movie marathon planning using
 * DP, Backtracking, and Branch & Bound.
 */
@RestController
@RequestMapping("/api/movies/marathon")
public class MovieMarathonController {

    private final MovieMarathonService service;

    public MovieMarathonController(MovieMarathonService service) {
        this.service = service;
    }

    /**
     * Plan a movie marathon using specified algorithm.
     * 
     * POST /api/movies/marathon/plan
     * Body: {
     *   "userId": "alice",
     *   "totalMinutes": 360,
     *   "minRating": 3.5,
     *   "preferredGenres": ["Sci-Fi", "Action"],
     *   "excludeMovies": ["movie_001"],
     *   "algorithm": "dp"
     * }
     * 
     * Algorithms: "dp", "backtracking", "branchandbound"
     */
    @PostMapping(
        value = "/plan",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public MarathonPlan planMarathon(@RequestBody MarathonRequest request) {
        return service.planMarathon(request);
    }

    /**
     * Get marathon suggestions using different algorithms.
     * 
     * GET /api/movies/marathon/suggestions?user=alice&totalMinutes=360
     */
    @GetMapping(
        value = "/suggestions",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> getMarathonSuggestions(
        @RequestParam("user") String user,
        @RequestParam(value = "totalMinutes", defaultValue = "360") int totalMinutes
    ) {
        return service.getMarathonSuggestions(user, totalMinutes);
    }

    /**
     * Quick marathon plan using DP (GET endpoint for convenience).
     * 
     * GET /api/movies/marathon/quick?user=alice&totalMinutes=360&minRating=3.5
     */
    @GetMapping(
        value = "/quick",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public MarathonPlan quickPlan(
        @RequestParam("user") String user,
        @RequestParam(value = "totalMinutes", defaultValue = "360") int totalMinutes,
        @RequestParam(value = "minRating", defaultValue = "3.0") double minRating,
        @RequestParam(value = "algorithm", defaultValue = "dp") String algorithm
    ) {
        MarathonRequest request = new MarathonRequest();
        request.userId = user;
        request.totalMinutes = totalMinutes;
        request.minRating = minRating;
        request.algorithm = algorithm;
        request.preferredGenres = List.of();
        request.excludeMovies = List.of();

        return service.planMarathon(request);
    }
}

