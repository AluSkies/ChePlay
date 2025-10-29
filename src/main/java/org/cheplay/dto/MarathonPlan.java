package org.cheplay.dto;

import java.util.List;
import java.util.Map;

public class MarathonPlan {
    public List<Map<String, Object>> movies;
    public Integer totalDuration;
    public Double averageRating;
    public Double totalScore;
    public String algorithm;

    public MarathonPlan() {}

    public MarathonPlan(
        List<Map<String, Object>> movies,
        Integer totalDuration,
        Double averageRating,
        Double totalScore,
        String algorithm
    ) {
        this.movies = movies;
        this.totalDuration = totalDuration;
        this.averageRating = averageRating;
        this.totalScore = totalScore;
        this.algorithm = algorithm;
    }
}

