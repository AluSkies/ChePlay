package org.cheplay;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.cheplay.dto.MarathonPlan;
import org.cheplay.dto.MarathonRequest;
import org.cheplay.model.marathon.MovieMarathonService;
import org.cheplay.neo4j.DbConnector;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/**
 * Tests for movie marathon planning using DP, Backtracking,
 * and Branch & Bound.
 */
public class MovieMarathonTest {

    @Test
    public void testMarathonPlanningDP() {
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
            MovieMarathonService service = new MovieMarathonService(db);

            MarathonRequest request = new MarathonRequest();
            request.userId = "alice";
            request.totalMinutes = 360;
            request.minRating = 3.0;
            request.algorithm = "dp";
            request.preferredGenres = List.of("Sci-Fi", "Action");
            request.excludeMovies = List.of();

            MarathonPlan plan = service.planMarathon(request);

            assertNotNull(plan);
            assertTrue(plan.totalDuration <= 360);
            System.out.println("DP Marathon Plan:");
            System.out.println("  Algorithm: " + plan.algorithm);
            System.out.println("  Total Duration: " + plan.totalDuration + " min");
            System.out.println("  Average Rating: " + plan.averageRating);
            System.out.println("  Movies: " + plan.movies.size());
        }
    }

    @Test
    public void testMarathonPlanningBranchAndBound() {
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
            MovieMarathonService service = new MovieMarathonService(db);

            MarathonRequest request = new MarathonRequest();
            request.userId = "alice";
            request.totalMinutes = 360;
            request.minRating = 3.5;
            request.algorithm = "branchandbound";
            request.preferredGenres = List.of();
            request.excludeMovies = List.of();

            MarathonPlan plan = service.planMarathon(request);

            assertNotNull(plan);
            assertTrue(plan.totalDuration <= 360);
            System.out.println("Branch & Bound Marathon Plan:");
            System.out.println("  Algorithm: " + plan.algorithm);
            System.out.println("  Total Duration: " + plan.totalDuration + " min");
            System.out.println("  Average Rating: " + plan.averageRating);
            System.out.println("  Movies: " + plan.movies.size());
        }
    }

    @Test
    public void testMarathonSuggestions() {
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
            MovieMarathonService service = new MovieMarathonService(db);

            Map<String, Object> suggestions =
                service.getMarathonSuggestions("alice", 360);

            assertNotNull(suggestions);
            assertTrue(suggestions.containsKey("dynamicProgramming"));
            assertTrue(suggestions.containsKey("branchAndBound"));
            System.out.println("Marathon suggestions retrieved successfully");
        }
    }
}

