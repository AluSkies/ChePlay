package org.cheplay.neo4j;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

/**
 * Utility class to seed Neo4j database with sample movie data,
 * user-movie interactions (WATCHED, RATED), and movie similarities.
 */
public class MovieDataSeeder {

    private final Driver driver;
    private final Random random = new Random(42);

    public MovieDataSeeder(Driver driver) {
        this.driver = driver;
    }

    public static void main(String[] args) {
        String uri = System.getenv()
            .getOrDefault("NEO4J_URI", "neo4j+s://868ceae8.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv()
            .getOrDefault("NEO4J_PASSWORD", "changeme");

        try (Driver driver = GraphDatabase.driver(
            uri,
            AuthTokens.basic(user, password)
        )) {
            MovieDataSeeder seeder = new MovieDataSeeder(driver);
            seeder.seedMovieData();
            System.out.println("Movie data seeded successfully!");
        }
    }

    public void seedMovieData() {
        try (Session session = driver.session()) {
            //createMovies(session);
            //createMovieSimilarities(session);
            createUserMovieInteractions(session);
        }
    }

    private void createMovies(Session session) {
        List<Map<String, Object>> movies = List.of(
            Map.of(
                "id", "movie_001",
                "title", "The Shawshank Redemption",
                "genre", "Drama",
                "year", 1994,
                "duration", 142
            ),
            Map.of(
                "id", "movie_002",
                "title", "The Godfather",
                "genre", "Crime",
                "year", 1972,
                "duration", 175
            ),
            Map.of(
                "id", "movie_003",
                "title", "The Dark Knight",
                "genre", "Action",
                "year", 2008,
                "duration", 152
            ),
            Map.of(
                "id", "movie_004",
                "title", "Pulp Fiction",
                "genre", "Crime",
                "year", 1994,
                "duration", 154
            ),
            Map.of(
                "id", "movie_005",
                "title", "Forrest Gump",
                "genre", "Drama",
                "year", 1994,
                "duration", 142
            ),
            Map.of(
                "id", "movie_006",
                "title", "Inception",
                "genre", "Sci-Fi",
                "year", 2010,
                "duration", 148
            ),
            Map.of(
                "id", "movie_007",
                "title", "The Matrix",
                "genre", "Sci-Fi",
                "year", 1999,
                "duration", 136
            ),
            Map.of(
                "id", "movie_008",
                "title", "Goodfellas",
                "genre", "Crime",
                "year", 1990,
                "duration", 146
            ),
            Map.of(
                "id", "movie_009",
                "title", "Interstellar",
                "genre", "Sci-Fi",
                "year", 2014,
                "duration", 169
            ),
            Map.of(
                "id", "movie_010",
                "title", "The Silence of the Lambs",
                "genre", "Thriller",
                "year", 1991,
                "duration", 118
            ),
            Map.of(
                "id", "movie_011",
                "title", "Parasite",
                "genre", "Thriller",
                "year", 2019,
                "duration", 132
            ),
            Map.of(
                "id", "movie_012",
                "title", "Spirited Away",
                "genre", "Animation",
                "year", 2001,
                "duration", 125
            ),
            Map.of(
                "id", "movie_013",
                "title", "The Lion King",
                "genre", "Animation",
                "year", 1994,
                "duration", 88
            ),
            Map.of(
                "id", "movie_014",
                "title", "Gladiator",
                "genre", "Action",
                "year", 2000,
                "duration", 155
            ),
            Map.of(
                "id", "movie_015",
                "title", "The Departed",
                "genre", "Crime",
                "year", 2006,
                "duration", 151
            ),
            Map.of(
                "id", "movie_016",
                "title", "Whiplash",
                "genre", "Drama",
                "year", 2014,
                "duration", 106
            ),
            Map.of(
                "id", "movie_017",
                "title", "The Prestige",
                "genre", "Thriller",
                "year", 2006,
                "duration", 130
            ),
            Map.of(
                "id", "movie_018",
                "title", "Django Unchained",
                "genre", "Western",
                "year", 2012,
                "duration", 165
            ),
            Map.of(
                "id", "movie_019",
                "title", "WALL-E",
                "genre", "Animation",
                "year", 2008,
                "duration", 98
            ),
            Map.of(
                "id", "movie_020",
                "title", "Avengers: Endgame",
                "genre", "Action",
                "year", 2019,
                "duration", 181
            )
        );

        String cypher = """
            UNWIND $movies AS movie
            MERGE (m:Movie {id: movie.id})
            SET m.title = movie.title,
                m.genre = movie.genre,
                m.year = movie.year,
                m.duration = movie.duration
            """;

        session.run(cypher, Map.of("movies", movies));
        System.out.println("Created " + movies.size() + " movies");
    }

    private void createMovieSimilarities(Session session) {
        List<Map<String, Object>> similarities = List.of(
            Map.of("from", "movie_001", "to", "movie_005", "score", 0.85),
            Map.of("from", "movie_001", "to", "movie_016", "score", 0.78),
            Map.of("from", "movie_002", "to", "movie_004", "score", 0.88),
            Map.of("from", "movie_002", "to", "movie_008", "score", 0.92),
            Map.of("from", "movie_002", "to", "movie_015", "score", 0.86),
            Map.of("from", "movie_003", "to", "movie_014", "score", 0.75),
            Map.of("from", "movie_003", "to", "movie_020", "score", 0.70),
            Map.of("from", "movie_004", "to", "movie_008", "score", 0.83),
            Map.of("from", "movie_004", "to", "movie_015", "score", 0.80),
            Map.of("from", "movie_006", "to", "movie_007", "score", 0.89),
            Map.of("from", "movie_006", "to", "movie_009", "score", 0.91),
            Map.of("from", "movie_006", "to", "movie_017", "score", 0.82),
            Map.of("from", "movie_007", "to", "movie_009", "score", 0.87),
            Map.of("from", "movie_010", "to", "movie_011", "score", 0.79),
            Map.of("from", "movie_010", "to", "movie_017", "score", 0.81),
            Map.of("from", "movie_012", "to", "movie_013", "score", 0.76),
            Map.of("from", "movie_012", "to", "movie_019", "score", 0.84),
            Map.of("from", "movie_013", "to", "movie_019", "score", 0.77),
            Map.of("from", "movie_014", "to", "movie_020", "score", 0.72),
            Map.of("from", "movie_005", "to", "movie_016", "score", 0.74)
        );

        String cypher = """
            UNWIND $similarities AS sim
            MATCH (a:Movie {id: sim.from})
            MATCH (b:Movie {id: sim.to})
            MERGE (a)-[r:SIMILAR_TO]->(b)
            SET r.score = sim.score
            MERGE (b)-[r2:SIMILAR_TO]->(a)
            SET r2.score = sim.score
            """;

        session.run(cypher, Map.of("similarities", similarities));
        System.out.println("Created " + similarities.size() + 
                           " movie similarity relationships");
    }

    private void createUserMovieInteractions(Session session) {
        String[] userIds = {"u1", "u2", "u3"};
        String[] movieIds = {
            "movie_001", "movie_002", "movie_003", "movie_004", "movie_005",
            "movie_006", "movie_007", "movie_008", "movie_009", "movie_010",
            "movie_011", "movie_012", "movie_013", "movie_014", "movie_015",
            "movie_016", "movie_017", "movie_018", "movie_019", "movie_020"
        };

        int watchedCount = 0;
        int ratedCount = 0;

        for (String userId : userIds) {
            session.run(
                "MERGE (u:User {id: $userId})",
                Map.of("userId", userId)
            );

            int numMoviesToWatch = 8 + random.nextInt(8);
            List<String> watchedMovies = randomSample(movieIds, numMoviesToWatch);

            for (String movieId : watchedMovies) {
                int watchCount = 1 + random.nextInt(5);
                session.run("""
                    MATCH (u:User {id: $userId})
                    MATCH (m:Movie {id: $movieId})
                    MERGE (u)-[w:WATCHED]->(m)
                    SET w.watchCount = $watchCount
                    """,
                    Map.of(
                        "userId", userId,
                        "movieId", movieId,
                        "watchCount", watchCount
                    )
                );
                watchedCount++;

                if (random.nextDouble() < 0.7) {
                    double rating = 2.0 + random.nextDouble() * 3.0;
                    rating = Math.round(rating * 2) / 2.0;
                    session.run("""
                        MATCH (u:User {id: $userId})
                        MATCH (m:Movie {id: $movieId})
                        MERGE (u)-[r:RATED]->(m)
                        SET r.rating = $rating
                        """,
                        Map.of(
                            "userId", userId,
                            "movieId", movieId,
                            "rating", rating
                        )
                    );
                    ratedCount++;
                }
            }
        }

        System.out.println("Created " + watchedCount + 
                           " WATCHED relationships");
        System.out.println("Created " + ratedCount + 
                           " RATED relationships");
    }

    private List<String> randomSample(String[] array, int count) {
        List<String> list = new java.util.ArrayList<>(List.of(array));
        java.util.Collections.shuffle(list, random);
        return list.subList(0, Math.min(count, list.size()));
    }
}

