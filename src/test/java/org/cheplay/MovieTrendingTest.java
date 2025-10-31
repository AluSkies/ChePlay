package org.cheplay;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.cheplay.dto.MovieWatch;
import org.cheplay.model.trending.MovieTrendingService;
import org.cheplay.neo4j.DbConnector;
import org.cheplay.neo4j.DynamicGraphAdapter;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/**
 * Tests for movie trending analytics using MergeSort, Greedy, and BFS.
 */
public class MovieTrendingTest {

    @Test
    public void testGlobalTrending() {
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
            MovieTrendingService service = new MovieTrendingService(db, adapter);

            List<MovieWatch> trending = service.topGlobalTrending(10);

            assertNotNull(trending);
            System.out.println("Global Trending Movies: " + trending.size());
            for (MovieWatch movie : trending) {
                System.out.println("  - " + movie.title +
                                   " [" + movie.genre + "]" +
                                   " - " + movie.watchCount + " views");
            }
        }
    }

    @Test
    public void testTrendingByGenre() {
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
            MovieTrendingService service = new MovieTrendingService(db, adapter);

            List<MovieWatch> trending = service.trendingMoviesByGenre("Sci-Fi", 5);

            assertNotNull(trending);
            System.out.println("Sci-Fi Trending Movies: " + trending.size());
        }
    }

    @Test
    public void testTrendingWithInfluence() {
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
            MovieTrendingService service = new MovieTrendingService(db, adapter);

            List<Map<String, Object>> influential =
                service.trendingWithInfluence(5);

            assertNotNull(influential);
            System.out.println("Influential Movies: " + influential.size());
        }
    }

    @Test
    public void testCompareUserTaste() {
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
            MovieTrendingService service = new MovieTrendingService(db, adapter);

            Map<String, Object> comparison =
                service.compareUserTaste("alice", 10);

            assertNotNull(comparison);
            assertTrue(comparison.containsKey("userTopMovies"));
            assertTrue(comparison.containsKey("globalTrending"));
            System.out.println("User taste comparison completed successfully");
        }
    }
}


