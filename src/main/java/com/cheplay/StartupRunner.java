package com.cheplay;

import java.util.List;

import org.cheplay.dto.SongPlay;
import org.cheplay.model.trending.TrendingGlobalService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
