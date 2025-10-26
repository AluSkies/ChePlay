package org.cheplay.neo4j;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {
    @Bean(destroyMethod = "close")
    public DbConnector dbConnector(Driver driver) {
        // Usa el Driver administrado por Spring (configurado con spring.neo4j.*)
        return DbConnector.from(driver);
    }
}
