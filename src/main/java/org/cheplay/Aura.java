package org.cheplay;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.net.InetAddress;
import java.net.URI;

public class Aura {
    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("NEO4J_URI", "neo4j+s://868ceae8.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "S8esVU08T9zrH69I0g3B8QFg4XEN6zMNWejH9GYwQ80");

        System.out.println("Trying to connect to Neo4j URI: " + uri);

        // Quick DNS check (helpful to surface resolution issues early)
        try {
            URI u = new URI(uri);
            String host = u.getHost();
            if (host == null) host = uri; // fallback
            System.out.println("Resolving host: " + host);
            InetAddress[] addrs = InetAddress.getAllByName(host);
            for (InetAddress a : addrs) System.out.println("Resolved address: " + a.getHostAddress());
        } catch (Exception e) {
            System.err.println("DNS/resolution check failed: " + e.getMessage());
        }

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {
            var result = session.run("RETURN 'Conexión exitosa con AuraDB' AS mensaje");
            System.out.println(result.single().get("mensaje").asString());
        } catch (Exception ex) {
            System.err.println("Neo4j connection failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}