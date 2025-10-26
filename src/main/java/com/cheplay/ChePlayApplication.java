package com.cheplay;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;


@SpringBootApplication(scanBasePackages = {"com.cheplay", "org.cheplay"})
@EnableNeo4jRepositories(basePackages = "org.cheplay.repository")
public class ChePlayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChePlayApplication.class, args);
    }
}