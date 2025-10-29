package org.cheplay.model.trending;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cheplay.algorithm.divideandconquer.MergeSort;
import org.cheplay.algorithm.graph.BFS;
import org.cheplay.algorithm.greedy.GreedyExamples;
import org.cheplay.dto.AlgorithmRequest;
import org.cheplay.dto.MovieWatch;
import org.cheplay.neo4j.DbConnector;
import org.springframework.stereotype.Service;

/**
 * Service for analyzing trending movies using MergeSort, Greedy, and BFS.
 */
@Service
public class MovieTrendingService {

    private final DbConnector db;
    private final org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter;

    public MovieTrendingService(
        DbConnector db,
        org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter
    ) {
        this.db = db;
        this.dynamicGraphAdapter = dynamicGraphAdapter;
    }

    /**
     * Get trending movies globally using Greedy Top-K.
     * 
     * Algorithm: Greedy
     */
    public List<MovieWatch> topGlobalTrending(int k) {
        if (k <= 0) {
            return List.of();
        }

        String cypher = """
            MATCH (m:Movie)<-[w:WATCHED]-(:User)
            WITH m, sum(w.watchCount) AS totalWatches
            RETURN m.id AS movieId, m.title AS title, m.genre AS genre,
                   m.year AS year, m.duration AS duration, totalWatches
            """;

        Map<String, Integer> movieWatches = new HashMap<>();
        Map<String, MovieWatch> index = new HashMap<>();

        List<org.neo4j.driver.Record> rows = db.readList(
            cypher,
            null,
            r -> r
        );

        for (org.neo4j.driver.Record row : rows) {
            String id = row.get("movieId").asString();
            String title = row.get("title").asString("");
            String genre = row.get("genre").asString("");
            Integer year = row.get("year").asInt(0);
            Integer duration = row.get("duration").asInt(0);
            int watches = row.get("totalWatches").asInt(0);

            movieWatches.put(id, watches);
            index.put(id, new MovieWatch(
                id,
                title,
                genre,
                year,
                duration,
                watches
            ));
        }

        if (movieWatches.isEmpty()) {
            return List.of();
        }

        List<Map.Entry<String, Integer>> topK =
            GreedyExamples.topKGreedy(movieWatches, k);

        return topK.stream()
            .map(entry -> {
                MovieWatch base = index.get(entry.getKey());
                return new MovieWatch(
                    entry.getKey(),
                    base != null ? base.title : "",
                    base != null ? base.genre : "",
                    base != null ? base.year : 0,
                    base != null ? base.duration : 0,
                    entry.getValue()
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Get trending movies by genre using Greedy + MergeSort.
     * 
     * Algorithm: Greedy + MergeSort
     */
    public List<MovieWatch> trendingMoviesByGenre(String genre, int k) {
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("genre is required");
        }
        if (k <= 0) {
            return List.of();
        }

        String cypher = """
            MATCH (m:Movie)<-[w:WATCHED]-(:User)
            WHERE m.genre = $genre
            WITH m, sum(w.watchCount) AS totalWatches
            RETURN m.id AS movieId, m.title AS title, m.genre AS genre,
                   m.year AS year, m.duration AS duration, totalWatches
            """;

        Map<String, Integer> movieWatches = new HashMap<>();
        Map<String, MovieWatch> index = new HashMap<>();

        List<org.neo4j.driver.Record> rows = db.readList(
            cypher,
            Map.of("genre", genre),
            r -> r
        );

        for (org.neo4j.driver.Record row : rows) {
            String id = row.get("movieId").asString();
            String title = row.get("title").asString("");
            String genreVal = row.get("genre").asString("");
            Integer year = row.get("year").asInt(0);
            Integer duration = row.get("duration").asInt(0);
            int watches = row.get("totalWatches").asInt(0);

            movieWatches.put(id, watches);
            index.put(id, new MovieWatch(
                id,
                title,
                genreVal,
                year,
                duration,
                watches
            ));
        }

        if (movieWatches.isEmpty()) {
            return List.of();
        }

        LinkedHashMap<String, Integer> sortedWatches =
            MergeSort.mergeSortByValue(movieWatches);

        return sortedWatches.entrySet().stream()
            .limit(k)
            .map(entry -> {
                MovieWatch base = index.get(entry.getKey());
                return new MovieWatch(
                    entry.getKey(),
                    base != null ? base.title : "",
                    base != null ? base.genre : "",
                    base != null ? base.year : 0,
                    base != null ? base.duration : 0,
                    entry.getValue()
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Get influential movies that drive most subsequent views.
     * Uses BFS to track influence propagation.
     * 
     * Algorithm: BFS (Influence Propagation)
     */
    public List<Map<String, Object>> trendingWithInfluence(int k) {
        if (k <= 0) {
            return List.of();
        }

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "movies_by_users";
        req.undirected = false;

        Map<String, Map<String, Double>> adj =
            dynamicGraphAdapter.buildAdjacency(req);

        Map<String, Integer> influenceScores = new HashMap<>();

        for (String movieId : adj.keySet()) {
            List<String> reachable = BFS.bfs(adj, movieId);
            influenceScores.put(movieId, reachable.size());
        }

        LinkedHashMap<String, Integer> sortedInfluence =
            MergeSort.mergeSortByValue(influenceScores);

        List<String> topMovies = sortedInfluence.keySet().stream()
            .limit(k)
            .collect(Collectors.toList());

        return getMovieDetailsWithInfluence(topMovies, sortedInfluence);
    }

    /**
     * Compare user taste with global trending.
     */
    public Map<String, Object> compareUserTaste(
        String userId,
        int k
    ) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }

        List<MovieWatch> globalTrending = topGlobalTrending(k);

        String userCypher = """
            MATCH (u:User)-[w:WATCHED]->(m:Movie)
            WHERE toLower(coalesce(u.id, u.nombre, u.name)) =
                  toLower($userId)
            RETURN m.id AS movieId, m.genre AS genre, sum(w.watchCount) AS watches
            ORDER BY watches DESC
            LIMIT $k
            """;

        List<Map<String, Object>> userWatched = db.readList(
            userCypher,
            Map.of("userId", userId, "k", k),
            record -> {
                Map<String, Object> result = new HashMap<>();
                result.put("movieId", record.get("movieId").asString());
                result.put("genre", record.get("genre").asString());
                result.put("watches", record.get("watches").asInt());
                return result;
            }
        );

        Map<String, Integer> userGenres = new HashMap<>();
        for (Map<String, Object> movie : userWatched) {
            String genre = (String) movie.get("genre");
            userGenres.merge(genre, 1, Integer::sum);
        }

        Map<String, Integer> trendingGenres = new HashMap<>();
        for (MovieWatch movie : globalTrending) {
            trendingGenres.merge(movie.genre, 1, Integer::sum);
        }

        return Map.of(
            "userTopMovies", userWatched,
            "userGenreDistribution", userGenres,
            "globalTrending", globalTrending,
            "trendingGenreDistribution", trendingGenres
        );
    }

    /**
     * Get movie details with influence scores.
     */
    private List<Map<String, Object>> getMovieDetailsWithInfluence(
        List<String> movieIds,
        Map<String, Integer> influenceScores
    ) {
        if (movieIds.isEmpty()) {
            return List.of();
        }

        String cypher = """
            UNWIND $movieIds AS mid
            MATCH (m:Movie {id: mid})
            RETURN m.id AS movieId, m.title AS title, m.genre AS genre,
                   m.year AS year
            """;

        return db.readList(
            cypher,
            Map.of("movieIds", movieIds),
            record -> {
                String movieId = record.get("movieId").asString();
                Map<String, Object> result = new HashMap<>();
                result.put("movieId", movieId);
                result.put("title", record.get("title").asString());
                result.put("genre", record.get("genre").asString());
                result.put("year", record.get("year").asInt());
                result.put(
                    "influenceScore",
                    influenceScores.getOrDefault(movieId, 0)
                );
                return result;
            }
        );
    }
}

