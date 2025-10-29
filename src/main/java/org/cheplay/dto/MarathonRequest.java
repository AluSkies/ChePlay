package org.cheplay.dto;

import java.util.List;

public class MarathonRequest {
    public String userId;
    public Integer totalMinutes;
    public Double minRating;
    public List<String> preferredGenres;
    public List<String> excludeMovies;
    public String algorithm;

    public MarathonRequest() {}

    public MarathonRequest(
        String userId,
        Integer totalMinutes,
        Double minRating,
        List<String> preferredGenres,
        List<String> excludeMovies,
        String algorithm
    ) {
        this.userId = userId;
        this.totalMinutes = totalMinutes;
        this.minRating = minRating;
        this.preferredGenres = preferredGenres;
        this.excludeMovies = excludeMovies;
        this.algorithm = algorithm;
    }
}

