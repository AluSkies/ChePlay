package org.cheplay.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

/**
 * Small CLI utility to seed a Neo4j database (Aura or local) with sample movie/user data.
 *
 * Usage (from project root):
 *   # set env vars or rely on application.properties placeholders
 *  $env:NEO4J_URI='neo4j+s://...'; $env:NEO4J_USER='neo4j'; $env:NEO4J_PASSWORD='secret'; \
 *   mvn -q -Dexec.mainClass=org.cheplay.neo4j.SeedDataLoader exec:java
 *
 * The loader uses MERGE statements so it's idempotent.
 */
public class SeedDataLoader {

    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("NEO4J_URI", System.getProperty("spring.neo4j.uri", "neo4j+s://868ceae8.databases.neo4j.io"));
        String user = System.getenv().getOrDefault("NEO4J_USER", System.getProperty("spring.neo4j.authentication.username", "neo4j"));
        String password = System.getenv().getOrDefault("NEO4J_PASSWORD", System.getProperty("spring.neo4j.authentication.password", "S8esVU08T9zrH69I0g3B8QFg4XEN6zMNWejH9GYwQ80"));

        System.out.println("Using Neo4j URI: " + uri);

    // Use the driver's default config. For encrypted schemes like neo4j+s the driver
    // will configure TLS and trust automatically; do not set manual encryption/trust.
    Config config = Config.defaultConfig();
    try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
             Session session = driver.session()) {

            // Use MERGE to make this idempotent
            session.run("MERGE (:Movie {id:'m1', title:'The Matrix', year:1999})");
            session.run("MERGE (:Movie {id:'m2', title:'Inception', year:2010})");
            session.run("MERGE (:Movie {id:'m3', title:'Interstellar', year:2014})");
            session.run("MERGE (:Movie {id:'m4', title:'The Godfather', year:1972})");
            session.run("MERGE (:Movie {id:'m5', title:'The Dark Knight', year:2008})");

            session.run("MERGE (:User {id:'u1', name:'Alice'})");
            session.run("MERGE (:User {id:'u2', name:'Bob'})");
            session.run("MERGE (:User {id:'u3', name:'Carol'})");

            session.run("MATCH (u:User {id:'u1'}), (m:Movie {id:'m1'}) MERGE (u)-[:RATED {rating:5}]->(m)");
            session.run("MATCH (u:User {id:'u1'}), (m:Movie {id:'m2'}) MERGE (u)-[:RATED {rating:4}]->(m)");
            session.run("MATCH (u:User {id:'u2'}), (m:Movie {id:'m1'}) MERGE (u)-[:RATED {rating:4}]->(m)");
            session.run("MATCH (u:User {id:'u2'}), (m:Movie {id:'m3'}) MERGE (u)-[:RATED {rating:5}]->(m)");
            session.run("MATCH (u:User {id:'u3'}), (m:Movie {id:'m3'}) MERGE (u)-[:RATED {rating:5}]->(m)");

            var movieCount = session.run("MATCH (m:Movie) RETURN count(m) AS c").single().get("c").asInt();
            var userCount = session.run("MATCH (u:User) RETURN count(u) AS c").single().get("c").asInt();
            var ratingCount = session.run("MATCH ()-[r:RATED]->() RETURN count(r) AS c").single().get("c").asInt();

            System.out.println("Seeded movies=" + movieCount + " users=" + userCount + " ratings=" + ratingCount);
        } catch (Exception e) {
            System.err.println("Failed to seed Neo4j: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
