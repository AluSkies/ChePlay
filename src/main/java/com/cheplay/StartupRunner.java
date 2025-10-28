package com.cheplay;

import java.util.List;
import java.util.Map;

import org.cheplay.dto.SongPlay;
import org.cheplay.model.trending.TrendingGlobalService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class StartupRunner {

    @Bean
    CommandLineRunner testTrending(TrendingGlobalService trending) {
        return args -> {
            System.out.println("▶ Probando Trending Global...");
            List<SongPlay> top10 = trending.topGlobalTrending(10, List.of()); // sin filtro
            if (top10.isEmpty()) {
                System.out.println("⚠ No hay datos LISTENED.");
                return;
            }
            System.out.println("=== Top 10 Global ===");
            for (int i = 0; i < top10.size(); i++) {
                var s = top10.get(i);
                System.out.printf("%2d. %-40s %-20s (%d plays)%n",
                        i + 1, s.title, s.artist, s.plays);
            }
        };
    }

    @Bean
    CommandLineRunner demoRecommendations(org.cheplay.model.recommendation.FriendRecommendationService recommendations) {
        return args -> {
            // If user supplied --no-recommend or --skip-recommend, do nothing
            for (String a : args) {
                if ("--no-recommend".equalsIgnoreCase(a) || "--skip-recommend".equalsIgnoreCase(a)) {
                    System.out.println("▶ StartupRunner: recomendaciones saltadas por argumento.");
                    return;
                }
            }

            // Default parameters
            // Use a DB key by default (u1) because users are referenced by their stored key/name
            String user = "u1";
            int k = 5;
            for (String a : args) {
                if (a.startsWith("--recommend.user=")) {
                    user = a.substring(a.indexOf('=') + 1);
                } else if (a.startsWith("--recommend.k=")) {
                    try {
                        k = Integer.parseInt(a.substring(a.indexOf('=') + 1));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            System.out.println("▶ Ejecutando demo de recomendaciones para: " + user + " (k=" + k + ")");
            try {
                var direct = recommendations.recommendDirectNeighbors(user, k);
                var shortest = recommendations.recommendByShortestPath(user, k);
                var closest = recommendations.findClosestUser(user);

        // Use a mutable map to allow null values (Map.of doesn't accept nulls)
        Map<String, Object> outMap = new java.util.HashMap<>();
        outMap.put("user", user);
        outMap.put("directRecommendations", direct);
        outMap.put("shortestPathRecommendations", shortest);
        outMap.put("closest", closest);

        ObjectMapper om = new ObjectMapper();
        System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(outMap));
            } catch (Throwable t) {
                System.out.println("⚠ Error al ejecutar recomendaciones en StartupRunner: " + t.getMessage());
                t.printStackTrace(System.out);
            }
        };
    }
}
