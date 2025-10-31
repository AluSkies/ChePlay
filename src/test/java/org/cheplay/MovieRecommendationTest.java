package org.cheplay;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.cheplay.model.recommendation.MovieRecommendationMapper;
import org.cheplay.model.recommendation.MovieRecommendationService;
import org.cheplay.neo4j.DbConnector;
import org.cheplay.neo4j.DynamicGraphAdapter;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/**
 * Tests for movie recommendation algorithms (BFS, DFS, Kruskal).
 */
public class MovieRecommendationTest {

    @Test
    public void testMovieRecommendationBFS() {
        String uri = System.getenv()
            .getOrDefault("NEO4J_URI", "neo4j+s://868ceae8.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv()
            .getOrDefault("NEO4J_PASSWORD", "changeme");

        try (Driver driver = GraphDatabase.driver(
            uri,
            AuthTokens.basic(user, password)
        )) {
            DbConnector db = DbConnector.from(driver);
            DynamicGraphAdapter adapter = new DynamicGraphAdapter(driver);
            MovieRecommendationMapper mapper = new MovieRecommendationMapper(db);
            MovieRecommendationService service =
                new MovieRecommendationService(adapter, db, mapper);

            List<Map<String, Object>> recommendations =
                service.recommendMoviesBFS("alice", 5, 3);

            assertNotNull(recommendations);
            System.out.println("BFS Recommendations: " + recommendations.size());
            for (Map<String, Object> rec : recommendations) {
                System.out.println("  - " + rec.get("title") +
                                   " [" + rec.get("genre") + "]" +
                                   " Score: " + rec.get("score"));
            }
        }
    }

    @Test
    public void testMovieRecommendationDFS() {
        String uri = System.getenv()
            .getOrDefault("NEO4J_URI", "neo4j+s://868ceae8.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv()
            .getOrDefault("NEO4J_PASSWORD", "changeme");

        try (Driver driver = GraphDatabase.driver(
            uri,
            AuthTokens.basic(user, password)
        )) {
            DbConnector db = DbConnector.from(driver);
            DynamicGraphAdapter adapter = new DynamicGraphAdapter(driver);
            MovieRecommendationMapper mapper = new MovieRecommendationMapper(db);
            MovieRecommendationService service =
                new MovieRecommendationService(adapter, db, mapper);

            List<Map<String, Object>> recommendations =
                service.recommendMoviesDFS("alice", 5, 4);

            assertNotNull(recommendations);
            System.out.println("DFS Recommendations: " + recommendations.size());
        }
    }

    @Test
    public void testMovieRecommendationDiverse() {
        String uri = System.getenv()
            .getOrDefault("NEO4J_URI", "neo4j+s://868ceae8.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv()
            .getOrDefault("NEO4J_PASSWORD", "changeme");

        try (Driver driver = GraphDatabase.driver(
            uri,
            AuthTokens.basic(user, password)
        )) {
            DbConnector db = DbConnector.from(driver);
            DynamicGraphAdapter adapter = new DynamicGraphAdapter(driver);
            MovieRecommendationMapper mapper = new MovieRecommendationMapper(db);
            MovieRecommendationService service =
                new MovieRecommendationService(adapter, db, mapper);

            List<Map<String, Object>> recommendations =
                service.recommendMoviesDiverse("alice", 5);

            assertNotNull(recommendations);
            System.out.println("Kruskal Diverse Recommendations: " +
                               recommendations.size());
        }
    }

    @Test
    public void testDebugWatchedData() {
        String uri = System.getenv()
            .getOrDefault("NEO4J_URI", "neo4j+s://868ceae8.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv()
            .getOrDefault("NEO4J_PASSWORD", "changeme");

        try (Driver driver = GraphDatabase.driver(
            uri,
            AuthTokens.basic(user, password)
        )) {
            DbConnector db = DbConnector.from(driver);
            DynamicGraphAdapter adapter = new DynamicGraphAdapter(driver);
            MovieRecommendationMapper mapper = new MovieRecommendationMapper(db);
            MovieRecommendationService service =
                new MovieRecommendationService(adapter, db, mapper);

            Map<String, Object> debugData =
                service.debugWatchedData("alice", 10);

            assertNotNull(debugData);
            assertTrue(debugData.containsKey("watchedMovies"));
            System.out.println("Debug data retrieved successfully");
        }
    }
}


