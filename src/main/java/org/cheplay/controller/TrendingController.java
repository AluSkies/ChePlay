package org.cheplay.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cheplay.dto.SongPlay;
import org.cheplay.model.trending.TrendingGlobalService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trending")
public class TrendingController {

    private final TrendingGlobalService trendingService;

    public TrendingController(TrendingGlobalService trendingService) {
        this.trendingService = trendingService;
    }

    @GetMapping(value = "/global", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SongPlay> topGlobal(
            @RequestParam(value = "k", defaultValue = "10") int k,
            @RequestParam(value = "platforms", required = false) List<String> platforms) {

        List<String> normalizedPlatforms = normalizePlatforms(platforms);
        return trendingService.topGlobalTrending(k, normalizedPlatforms);
    }

    private List<String> normalizePlatforms(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            Arrays.stream(entry.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .filter(s -> !out.contains(s))
                  .forEach(out::add);
        }
        return out;
    }
}
