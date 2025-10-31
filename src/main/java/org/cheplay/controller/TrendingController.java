package org.cheplay.controller;

import java.util.List;

import org.cheplay.controller.trending.TrendingRequest;
import org.cheplay.controller.trending.TrendingRequestFactory;
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
    private final TrendingRequestFactory requestFactory;

    public TrendingController(TrendingGlobalService trendingService,
                              TrendingRequestFactory requestFactory) {
        this.trendingService = trendingService;
        this.requestFactory = requestFactory;
    }

    @GetMapping(value = "/global", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SongPlay> topGlobal(
            @RequestParam(value = "k", defaultValue = "10") int k,
            @RequestParam(value = "platforms", required = false) List<String> platforms) {
        TrendingRequest request = requestFactory.create(k, platforms);
        return trendingService.topGlobalTrending(request.limit(), request.platforms());
    }
}
