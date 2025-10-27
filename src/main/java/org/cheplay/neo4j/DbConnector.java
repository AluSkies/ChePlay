package org.cheplay.neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionContext;

public class DbConnector implements AutoCloseable {
    private final String uri, user, pass;
    private Driver driver;

    private DbConnector(String uri, String user, String pass) {
        this.uri = Objects.requireNonNull(uri, "uri");
        this.user = Objects.requireNonNull(user, "user");
        this.pass = Objects.requireNonNull(pass, "pass");
    }

    // Alternate constructor for using an already-configured Driver (preferred with Spring Boot)
    private DbConnector(Driver driver) {
        this.uri = null;
        this.user = null;
        this.pass = null;
        this.driver = Objects.requireNonNull(driver, "driver");
    }

    public static DbConnector from(String uri, String user, String pass) {
        return new DbConnector(uri, user, pass);
    }

    public static DbConnector from(Driver driver) {
        return new DbConnector(driver);
    }

    // Lee de variables de entorno: NEO4J_URI / NEO4J_USER / NEO4J_PASSWORD
    public static DbConnector fromEnv() {
        String uri  = System.getenv().getOrDefault("NEO4J_URI", "neo4j+s://<TU_URI>.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String pass = System.getenv().getOrDefault("NEO4J_PASSWORD", "changeme");
        return new DbConnector(uri, user, pass);
    }

    public DbConnector open() {
        // Only create a Driver if not provided externally
        if (driver == null) driver = GraphDatabase.driver(uri, AuthTokens.basic(user, pass));
        return this;
    }

    public <T> T read(Function<TransactionContext, T> work) {
        ensureOpen();
        try (Session s = driver.session(SessionConfig.defaultConfig())) {
            return s.executeRead(work::apply);
        }
    }

    public <T> T write(Function<TransactionContext, T> work) {
        ensureOpen();
        try (Session s = driver.session(SessionConfig.defaultConfig())) {
            return s.executeWrite(work::apply);
        }
    }

    public <T> List<T> readList(String cypher, Map<String,Object> params,
                                java.util.function.Function<org.neo4j.driver.Record, T> mapper) {
        return read(tx -> {
            Result res = tx.run(cypher, params == null ? Map.of() : params);
            var rows = res.list();
            List<T> out = new ArrayList<>(rows.size());
            for (org.neo4j.driver.Record r : rows) out.add(mapper.apply(r));
            return out;
        });
    }

    private void ensureOpen() {
        if (driver == null) throw new IllegalStateException("DbConnector no está abierto. Llamá a open().");
    }

    @Override public void close() {
        if (driver != null) { driver.close(); driver = null; }
    }
}
