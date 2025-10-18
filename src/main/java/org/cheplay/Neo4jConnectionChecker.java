package org.cheplay;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

@Component
public class Neo4jConnectionChecker {

    private final Driver driver;

    public Neo4jConnectionChecker(Driver driver) {
        this.driver = driver;
    }

    @Bean
    CommandLineRunner checkConnection() {
        return args -> {
            try {
                try (Session session = driver.session()) {
                    var result = session.run("RETURN 'Conectado a AuraDB ✅' AS mensaje");
                    System.out.println(result.single().get("mensaje").asString());
                }
                try (Session session = driver.session()) {
                    session.run("CREATE (p:Persona {nombre:'Gus', rol:'AI Engineer'})");
                    var r = session.run("MATCH (p:Persona) RETURN count(p) AS total");
                    System.out.println("Nodos Persona: " + r.single().get("total").asInt());
                }
            } catch (Exception ex) {
                System.err.println("No se pudo conectar a Neo4j en el arranque: " + ex.getMessage());
                // don't rethrow; allow app to continue even if DB is unreachable
            }
        };
    }
}

