package org.cheplay;

import org.neo4j.driver.*;

public class Aura {
    public static void main(String[] args) {
        String uri = "neo4j+s://868ceae8.databases.neo4j.io";
        String user = "neo4j";
        String password = "S8esVU08T9zrH69I0g3B8QFg4XEN6zMNWejH9GYwQ80";

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {
            var result = session.run("RETURN 'Conexión exitosa con AuraDB' AS mensaje");
            System.out.println(result.single().get("mensaje").asString());
        }
    }
}