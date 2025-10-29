package org.cheplay.model.recommendation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.cheplay.neo4j.DbConnector;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Component;

/**
 * Mapper to decorate movie recommendation results with metadata
 * from Neo4j database.
 */
@Component
public class MovieRecommendationMapper {

    private final DbConnector db;

    public MovieRecommendationMapper(DbConnector db) {
        this.db = Objects.requireNonNull(db, "DbConnector");
    }

    /**
     * Convert map of movieId -> score to list of decorated recommendations.
     */
    public List<Map<String, Object>> toRecommendationList(
        Map<String, Double> scores,
        int k
    ) {
        List<Map.Entry<String, Double>> ranked = scores.entrySet().stream()
            .sorted(Comparator.comparingDouble(Map.Entry::getValue))
            .limit(k <= 0 ? Integer.MAX_VALUE : k)
            .collect(Collectors.toList());

        return decorateMovies(ranked);
    }

    /**
     * Decorate movie entries with metadata from database.
     */
    private List<Map<String, Object>> decorateMovies(
        List<Map.Entry<String, Double>> ranked
    ) {
        if (ranked.isEmpty()) {
            return List.of();
        }

        List<String> movieIds = ranked.stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        Map<String, Map<String, Object>> movieData = fetchMovieData(movieIds);

        return ranked.stream()
            .map(entry -> {
                String movieId = entry.getKey();
                Double score = entry.getValue();
                Map<String, Object> data = movieData
                    .getOrDefault(movieId, Map.of());

                Map<String, Object> result = new HashMap<>();
                result.put("id", movieId);
                result.put("score", score);
                result.put("title", data.getOrDefault("title", movieId));
                result.put("genre", data.get("genre"));
                result.put("year", data.get("year"));
                result.put("duration", data.get("duration"));

                String title = (String) data.get("title");
                String genre = (String) data.get("genre");
                String label = (title != null ? title : movieId) +
                    (genre != null ? " (" + genre + ")" : "");
                result.put("label", label);

                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * Fetch movie metadata from database for given movie IDs.
     */
    private Map<String, Map<String, Object>> fetchMovieData(
        List<String> movieIds
    ) {
        if (movieIds == null || movieIds.isEmpty()) {
            return Map.of();
        }

        String cypher = """
            UNWIND $movieIds AS mid
            MATCH (m:Movie {id: mid})
            RETURN m.id AS id, m.title AS title, m.genre AS genre,
                   m.year AS year, m.duration AS duration
            """;

        List<Map<String, Object>> rows = db.readList(
            cypher,
            Map.of("movieIds", movieIds),
            (Record r) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("id", r.get("id").asString());
                if (!r.get("title").isNull()) {
                    row.put("title", r.get("title").asString());
                }
                if (!r.get("genre").isNull()) {
                    row.put("genre", r.get("genre").asString());
                }
                if (!r.get("year").isNull()) {
                    row.put("year", r.get("year").asInt());
                }
                if (!r.get("duration").isNull()) {
                    row.put("duration", r.get("duration").asInt());
                }
                return row;
            }
        );

        return rows.stream()
            .collect(Collectors.toMap(
                row -> (String) row.get("id"),
                row -> row,
                (a, b) -> a
            ));
    }

    /**
     * Decorate list of movie IDs with full metadata.
     */
    public List<Map<String, Object>> decorateMovieIds(List<String> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return List.of();
        }

        Map<String, Map<String, Object>> movieData = fetchMovieData(movieIds);

        return movieIds.stream()
            .map(movieId -> {
                Map<String, Object> data = movieData
                    .getOrDefault(movieId, Map.of());
                Map<String, Object> result = new HashMap<>();
                result.put("id", movieId);
                result.put("title", data.getOrDefault("title", movieId));
                result.put("genre", data.get("genre"));
                result.put("year", data.get("year"));
                result.put("duration", data.get("duration"));
                return result;
            })
            .collect(Collectors.toList());
    }
}

