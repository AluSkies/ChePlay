package org.cheplay.model.recommendation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.cheplay.algorithm.divideandconquer.MergeSort;
import org.cheplay.algorithm.graph.BFS;
import org.cheplay.algorithm.graph.DFS;
import org.cheplay.algorithm.mst.Kruskal;
import org.cheplay.dto.AlgorithmRequest;
import org.cheplay.neo4j.DbConnector;
import org.springframework.stereotype.Service;

/**
 * Service for movie recommendations using graph algorithms.
 * Implements BFS, DFS, and Kruskal MST for diverse recommendation strategies.
 */
@Service
public class MovieRecommendationService {

    private final org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter;
    private final DbConnector db;
    private final MovieRecommendationMapper mapper;

    public MovieRecommendationService(
        org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter,
        DbConnector db,
        MovieRecommendationMapper mapper
    ) {
        this.dynamicGraphAdapter = Objects.requireNonNull(dynamicGraphAdapter);
        this.db = Objects.requireNonNull(db);
        this.mapper = Objects.requireNonNull(mapper);
    }

    /**
     * Recommend movies using BFS algorithm.
     * Explores movies layer-by-layer from user's watched movies,
     * ensuring recommendations aren't too obscure.
     * 
     * Algorithm: BFS (Breadth-First Search)
     */
    public List<Map<String, Object>> recommendMoviesBFS(
        String userId,
        int k,
        Integer maxDepth
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        List<String> watchedMovies = getUserWatchedMovies(userId);

        if (watchedMovies == null || watchedMovies.isEmpty()) {
            return List.of();
        }

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "movies_by_users";
        req.undirected = true;

        Map<String, Map<String, Double>> adj =
            dynamicGraphAdapter.buildAdjacency(req);

        Set<String> exclude = new HashSet<>(watchedMovies);
        Map<String, Integer> candidates = new HashMap<>();

        for (String seed : watchedMovies) {
            if (!adj.containsKey(seed)) {
                continue;
            }

            List<String> bfsOrder = BFS.bfs(adj, seed);

            // Process all nodes from BFS, but give higher scores to closer ones
            for (int i = 0; i < bfsOrder.size(); i++) {
                String movieId = bfsOrder.get(i);
                if (!exclude.contains(movieId)) {
                    // Score based on position: earlier = higher score
                    int score = Math.max(1, bfsOrder.size() - i);
                    candidates.merge(movieId, score, Integer::sum);
                }
            }
        }

        Map<String, Double> scores = candidates.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> 1.0 / (e.getValue() + 1.0)
            ));

        return mapper.toRecommendationList(scores, k);
    }

    /**
     * Recommend movies using DFS algorithm.
     * Deep exploration of movie similarity chains to find hidden gems.
     * Great for users wanting to explore niche subgenres.
     * 
     * Algorithm: DFS (Depth-First Search)
     */
    public List<Map<String, Object>> recommendMoviesDFS(
        String userId,
        int k,
        Integer maxDepth
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        List<String> watchedMovies = getUserWatchedMovies(userId);

        if (watchedMovies == null || watchedMovies.isEmpty()) {
            return List.of();
        }

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "movies_by_users";
        req.undirected = true;

        Map<String, Map<String, Double>> adj =
            dynamicGraphAdapter.buildAdjacency(req);

        Set<String> exclude = new HashSet<>(watchedMovies);
        Map<String, Integer> candidates = new HashMap<>();

        for (String seed : watchedMovies) {
            if (!adj.containsKey(seed)) {
                continue;
            }

            Set<String> visited = new HashSet<>();
            List<String> dfsOrder = new ArrayList<>();
            DFS.dfs(seed, adj, visited, dfsOrder);

            // Process all nodes from DFS, but give higher scores to closer ones
            for (int i = 0; i < dfsOrder.size(); i++) {
                String movieId = dfsOrder.get(i);
                if (!exclude.contains(movieId)) {
                    // Score based on position: earlier = higher score
                    int score = Math.max(1, dfsOrder.size() - i);
                    candidates.merge(movieId, score, Integer::sum);
                }
            }
        }

        Map<String, Double> scores = candidates.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> 1.0 / (e.getValue() + 1.0)
            ));

        return mapper.toRecommendationList(scores, k);
    }

    /**
     * Recommend diverse movies using Kruskal MST algorithm.
     * Builds minimum spanning tree to ensure variety without redundancy.
     * Prevents "5 similar action movies" syndrome.
     * 
     * Algorithm: Kruskal (Minimum Spanning Tree)
     */
    public List<Map<String, Object>> recommendMoviesDiverse(
        String userId,
        int k
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        List<String> watchedMovies = getUserWatchedMovies(userId);

        if (watchedMovies == null || watchedMovies.isEmpty()) {
            return List.of();
        }

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "movies_by_users";
        req.undirected = true;

        Map<String, Map<String, Double>> adj =
            dynamicGraphAdapter.buildAdjacency(req);

        if (adj.isEmpty()) {
            return List.of();
        }

        Map<String, Object> mstResult = Kruskal.minimumSpanningTree(adj);
        @SuppressWarnings("unchecked")
        List<Kruskal.Edge> mstEdges = (List<Kruskal.Edge>)
            mstResult.getOrDefault("mst", List.of());

        Set<String> exclude = new HashSet<>(watchedMovies);
        Map<String, Double> candidates = new HashMap<>();

        for (Kruskal.Edge edge : mstEdges) {
            if (!exclude.contains(edge.from)) {
                candidates.merge(edge.from, edge.weight, Double::sum);
            }
            if (!exclude.contains(edge.to)) {
                candidates.merge(edge.to, edge.weight, Double::sum);
            }
        }

        return mapper.toRecommendationList(candidates, k);
    }

    /**
     * Recommend movies filtered by genre using BFS.
     * 
     * Algorithm: BFS with genre filtering
     */
    public List<Map<String, Object>> recommendMoviesByGenre(
        String userId,
        String genre,
        int k
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("genre is required");
        }

        List<String> watchedMovies = getUserWatchedMovies(userId);

        if (watchedMovies == null || watchedMovies.isEmpty()) {
            return List.of();
        }

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "movies_genre";
        req.undirected = true;
        req.params = Map.of("genre", genre);

        Map<String, Map<String, Double>> adj =
            dynamicGraphAdapter.buildAdjacency(req);

        Set<String> exclude = new HashSet<>(watchedMovies);
        Map<String, Integer> candidates = new HashMap<>();

        for (String seed : watchedMovies) {
            if (!adj.containsKey(seed)) {
                continue;
            }

            List<String> bfsOrder = BFS.bfs(adj, seed);

            for (int i = 0; i < bfsOrder.size() && i < 4; i++) {
                String movieId = bfsOrder.get(i);
                if (!exclude.contains(movieId)) {
                    candidates.merge(movieId, 4 - i, Integer::sum);
                }
            }
        }

        Map<String, Double> scores = candidates.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> 1.0 / (e.getValue() + 1.0)
            ));

        return mapper.toRecommendationList(scores, k);
    }

    /**
     * Recommend movies sorted using MergeSort algorithm.
     * Sorts by composite score combining rating, watch count, and similarity.
     * 
     * Algorithm: MergeSort
     */
    public List<Map<String, Object>> recommendMoviesSorted(
        String userId,
        int k
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        List<String> watchedMovies = getUserWatchedMovies(userId);

        if (watchedMovies == null || watchedMovies.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> moviesWithRatings =
            getMoviesWithUserRatings(userId);

        Map<String, Integer> scores = new HashMap<>();
        for (Map<String, Object> movie : moviesWithRatings) {
            String movieId = (String) movie.get("movieId");
            Double rating = (Double) movie.get("rating");
            Integer watchCount = (Integer) movie.get("watchCount");

            if (rating != null && watchCount != null) {
                int compositeScore = (int) (rating * 100) + watchCount;
                scores.put(movieId, compositeScore);
            }
        }

        LinkedHashMap<String, Integer> sortedScores =
            MergeSort.mergeSortByValue(scores);

        List<String> topMovies = sortedScores.keySet().stream()
            .filter(id -> !watchedMovies.contains(id))
            .limit(k <= 0 ? Integer.MAX_VALUE : k)
            .collect(Collectors.toList());

        return mapper.decorateMovieIds(topMovies);
    }

    /**
     * Get list of movies user has watched.
     */
    private List<String> getUserWatchedMovies(String userId) {
        String cypher = """
            MATCH (u:User)
            WHERE toLower(coalesce(u.id, u.nombre, u.name)) =
                  toLower($userId)
            MATCH (u)-[:WATCHED]->(m:Movie)
            RETURN DISTINCT m.id AS movieId
            ORDER BY movieId
            """;

        return db.readList(cypher, Map.of("userId", userId), record -> {
            if (record == null || record.get("movieId").isNull()) {
                return null;
            }
            return record.get("movieId").asString();
        }).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Get movies with user ratings and watch counts.
     */
    private List<Map<String, Object>> getMoviesWithUserRatings(
        String userId
    ) {
        String cypher = """
            MATCH (m:Movie)
            OPTIONAL MATCH (u:User)-[w:WATCHED]->(m)
            WHERE toLower(coalesce(u.id, u.nombre, u.name)) =
                  toLower($userId)
            OPTIONAL MATCH (u)-[r:RATED]->(m)
            WITH m, avg(r.rating) AS avgRating, sum(w.watchCount) AS watches
            WHERE avgRating IS NOT NULL OR watches IS NOT NULL
            RETURN m.id AS movieId,
                   coalesce(avgRating, 0.0) AS rating,
                   coalesce(watches, 0) AS watchCount
            """;

        return db.readList(cypher, Map.of("userId", userId), record -> {
            Map<String, Object> result = new HashMap<>();
            result.put("movieId", record.get("movieId").asString());
            result.put("rating", record.get("rating").asDouble());
            result.put("watchCount", record.get("watchCount").asInt());
            return result;
        });
    }

    /**
     * Debug method to inspect user's watched movies data.
     */
    public Map<String, Object> debugWatchedData(String userId, int limit) {
        int cappedLimit = limit <= 0 ? 20 : Math.min(limit, 200);

        List<String> watchedAll = getUserWatchedMovies(userId);

        String watchesCypher = """
            MATCH (u:User)
            WHERE toLower(coalesce(u.id, u.nombre, u.name)) =
                  toLower($userId)
            MATCH (u)-[w:WATCHED]->(m:Movie)
            OPTIONAL MATCH (u)-[r:RATED]->(m)
            RETURN m.id AS movieId, m.title AS title, m.genre AS genre,
                   w.watchCount AS watchCount, r.rating AS rating
            ORDER BY coalesce(w.watchCount, 0) DESC, movieId ASC
            LIMIT $limit
            """;

        List<Map<String, Object>> watched = db.readList(
            watchesCypher,
            Map.of("userId", userId, "limit", cappedLimit),
            record -> {
                Map<String, Object> row = new HashMap<>();
                row.put("movieId", record.get("movieId").asString());
                row.put("title", record.get("title").asString());
                row.put("genre", record.get("genre").asString());
                if (!record.get("watchCount").isNull()) {
                    row.put("watchCount", record.get("watchCount").asInt());
                }
                if (!record.get("rating").isNull()) {
                    row.put("rating", record.get("rating").asDouble());
                }
                return row;
            }
        );

        Map<String, Object> out = new HashMap<>();
        out.put("user", userId);
        out.put("watchedMovies", watchedAll);
        out.put("watchedSample", watched);
        out.put("watchedCount", watchedAll != null ? watchedAll.size() : 0);

        return out;
    }
}

