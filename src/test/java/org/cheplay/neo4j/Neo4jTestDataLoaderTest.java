package org.cheplay.neo4j;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Neo4jContainer;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Neo4jTestDataLoaderTest {

    @Test
    public void populateMovieGraph_and_basicQuery() {
        // Requires Docker to be available locally for Testcontainers
        try (Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.7")) {
            neo4j.start();

            Config config = Config.builder().withEncryption().build();
            try (Driver driver = GraphDatabase.driver(neo4j.getBoltUrl(),
                    AuthTokens.basic("neo4j", neo4j.getAdminPassword()), config);
                 Session session = driver.session()) {

                // Create movies
                session.run("CREATE (:Movie {id:'m1', title:'The Matrix', year:1999})");
                session.run("CREATE (:Movie {id:'m2', title:'Inception', year:2010})");
                session.run("CREATE (:Movie {id:'m3', title:'Interstellar', year:2014})");

                // Create users
                session.run("CREATE (:User {id:'u1', name:'Alice'})");
                session.run("CREATE (:User {id:'u2', name:'Bob'})");
                session.run("CREATE (:User {id:'u3', name:'Carol'})");

                // Create ratings relationships
                session.run("MATCH (u:User {id:'u1'}), (m:Movie {id:'m1'}) CREATE (u)-[:RATED {rating:5}]->(m)");
                session.run("MATCH (u:User {id:'u1'}), (m:Movie {id:'m2'}) CREATE (u)-[:RATED {rating:4}]->(m)");
                session.run("MATCH (u:User {id:'u2'}), (m:Movie {id:'m1'}) CREATE (u)-[:RATED {rating:4}]->(m)");
                session.run("MATCH (u:User {id:'u2'}), (m:Movie {id:'m3'}) CREATE (u)-[:RATED {rating:5}]->(m)");
                session.run("MATCH (u:User {id:'u3'}), (m:Movie {id:'m3'}) CREATE (u)-[:RATED {rating:5}]->(m)");

                // Verify counts
                int movieCount = session.run("MATCH (m:Movie) RETURN count(m) AS c").single().get("c").asInt();
                int userCount = session.run("MATCH (u:User) RETURN count(u) AS c").single().get("c").asInt();
                int ratingCount = session.run("MATCH ()-[r:RATED]->() RETURN count(r) AS c").single().get("c").asInt();

                assertEquals(3, movieCount);
                assertEquals(3, userCount);
                assertEquals(5, ratingCount);

                // Example recommendation-like query: movies liked by users who liked m1
                var recs = session.run(
                        "MATCH (u:User)-[r:RATED]->(m:Movie {id:'m1'}) WHERE r.rating >= 4 " +
                                "MATCH (u)-[r2:RATED]->(other:Movie) WHERE other.id <> 'm1' " +
                                "RETURN other.id AS movie, avg(r2.rating) AS score " +
                                "ORDER BY score DESC");

                // There should be at least one recommendation (m2 or m3 depending on data)
                assertEquals(true, recs.hasNext());
            }
        }
    }
}
