package org.cheplay.model.marathon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.cheplay.dto.MarathonPlan;
import org.cheplay.dto.MarathonRequest;
import org.cheplay.neo4j.DbConnector;
import org.springframework.stereotype.Service;

/**
 * Service for planning movie marathons using optimization algorithms.
 */
@Service
public class MovieMarathonService {

    private final DbConnector db;

    public MovieMarathonService(DbConnector db) {
        this.db = Objects.requireNonNull(db, "DbConnector");
    }

    /**
     * Plan a movie marathon using specified algorithm.
     */
    public MarathonPlan planMarathon(MarathonRequest request) {
        if (request == null || request.userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.totalMinutes == null || request.totalMinutes <= 0) {
            throw new IllegalArgumentException("totalMinutes must be positive");
        }

        String algorithm = request.algorithm != null ?
            request.algorithm : "dp";
        double minRating = request.minRating != null ?
            request.minRating : 0.0;

        List<Map<String, Object>> availableMovies =
            getAvailableMoviesForUser(
                request.userId,
                request.excludeMovies,
                minRating
            );

        for (Map<String, Object> movie : availableMovies) {
            double score = MovieMarathonPlanner.calculateMovieScore(
                movie,
                request.preferredGenres
            );
            movie.put("score", score);
        }

        Map<String, Object> result;

        switch (algorithm.toLowerCase()) {
            case "backtracking":
                result = MovieMarathonPlanner.findSequencesBacktracking(
                    availableMovies,
                    request.totalMinutes,
                    minRating
                );
                break;
            case "branchandbound":
            case "branch-and-bound":
                result = MovieMarathonPlanner.optimizeMarathonBranchAndBound(
                    availableMovies,
                    request.totalMinutes,
                    minRating
                );
                break;
            case "dp":
            default:
                result = MovieMarathonPlanner.planMarathonDP(
                    availableMovies,
                    request.totalMinutes
                );
                break;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> movies = (List<Map<String, Object>>)
            result.getOrDefault("movies", List.of());
        Integer totalDuration = (Integer)
            result.getOrDefault("totalDuration", 0);
        Double totalScore = (Double) result.getOrDefault("totalScore", 0.0);
        String algo = (String) result.getOrDefault("algorithm", algorithm);

        double avgRating = 0.0;
        if (!movies.isEmpty()) {
            avgRating = movies.stream()
                .mapToDouble(m -> (Double) m.getOrDefault("rating", 0.0))
                .average()
                .orElse(0.0);
        }

        return new MarathonPlan(
            movies,
            totalDuration,
            avgRating,
            totalScore,
            algo
        );
    }

    /**
     * Get available movies for user excluding already watched or excluded.
     */
    private List<Map<String, Object>> getAvailableMoviesForUser(
        String userId,
        List<String> excludeMovies,
        double minRating
    ) {
        String cypher = """
            MATCH (m:Movie)
            OPTIONAL MATCH (u:User)-[:WATCHED]->(m)
            WHERE toLower(coalesce(u.id, u.nombre, u.name)) =
                  toLower($userId)
            WITH m, count(u) AS watchedByUser
            OPTIONAL MATCH (anyUser:User)-[r:RATED]->(m)
            WITH m, watchedByUser, avg(r.rating) AS avgRating
            WHERE watchedByUser = 0
              AND coalesce(avgRating, 0.0) >= $minRating
              AND NOT m.id IN $excludeList
            RETURN m.id AS movieId, m.title AS title, m.genre AS genre,
                   m.year AS year, m.duration AS duration,
                   coalesce(avgRating, 0.0) AS rating
            ORDER BY avgRating DESC, title ASC
            """;

        List<String> exclude = excludeMovies != null ?
            excludeMovies : List.of();

        return db.readList(
            cypher,
            Map.of(
                "userId", userId,
                "minRating", minRating,
                "excludeList", exclude
            ),
            record -> {
                Map<String, Object> movie = new HashMap<>();
                movie.put("movieId", record.get("movieId").asString());
                movie.put("title", record.get("title").asString());
                movie.put("genre", record.get("genre").asString());
                movie.put("year", record.get("year").asInt());
                movie.put("duration", record.get("duration").asInt());
                movie.put("rating", record.get("rating").asDouble());
                return movie;
            }
        );
    }

    /**
     * Get marathon suggestions for user with different algorithms.
     */
    public Map<String, Object> getMarathonSuggestions(
        String userId,
        int totalMinutes
    ) {
        MarathonRequest baseRequest = new MarathonRequest();
        baseRequest.userId = userId;
        baseRequest.totalMinutes = totalMinutes;
        baseRequest.minRating = 3.0;

        MarathonRequest dpRequest = new MarathonRequest();
        dpRequest.userId = userId;
        dpRequest.totalMinutes = totalMinutes;
        dpRequest.minRating = 3.0;
        dpRequest.algorithm = "dp";

        MarathonRequest bbRequest = new MarathonRequest();
        bbRequest.userId = userId;
        bbRequest.totalMinutes = totalMinutes;
        bbRequest.minRating = 3.5;
        bbRequest.algorithm = "branchandbound";

        MarathonPlan dpPlan = planMarathon(dpRequest);
        MarathonPlan bbPlan = planMarathon(bbRequest);

        return Map.of(
            "dynamicProgramming", dpPlan,
            "branchAndBound", bbPlan
        );
    }
}

