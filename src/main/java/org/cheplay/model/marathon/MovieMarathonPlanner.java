package org.cheplay.model.marathon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Algorithms for planning movie marathons using DP, Backtracking,
 * and Branch & Bound.
 */
public class MovieMarathonPlanner {

    /**
     * Plan movie marathon using Dynamic Programming (Knapsack variant).
     * Maximizes enjoyment score within time constraint.
     * 
     * Algorithm: Dynamic Programming (0/1 Knapsack)
     */
    public static Map<String, Object> planMarathonDP(
        List<Map<String, Object>> availableMovies,
        int totalMinutes
    ) {
        if (availableMovies == null || availableMovies.isEmpty()) {
            return Map.of(
                "movies", List.of(),
                "totalDuration", 0,
                "totalScore", 0.0,
                "algorithm", "DP"
            );
        }

        int n = availableMovies.size();
        double[][] dp = new double[n + 1][totalMinutes + 1];

        for (int i = 1; i <= n; i++) {
            Map<String, Object> movie = availableMovies.get(i - 1);
            int duration = (Integer) movie.get("duration");
            double score = (Double) movie.get("score");

            for (int t = 0; t <= totalMinutes; t++) {
                dp[i][t] = dp[i - 1][t];
                if (duration <= t) {
                    dp[i][t] = Math.max(
                        dp[i][t],
                        dp[i - 1][t - duration] + score
                    );
                }
            }
        }

        List<Integer> chosenIndices = new ArrayList<>();
        int t = totalMinutes;
        for (int i = n; i > 0; i--) {
            if (dp[i][t] != dp[i - 1][t]) {
                chosenIndices.add(i - 1);
                Map<String, Object> movie = availableMovies.get(i - 1);
                t -= (Integer) movie.get("duration");
            }
        }

        List<Map<String, Object>> chosenMovies = new ArrayList<>();
        int totalDuration = 0;
        double totalScore = 0.0;

        for (int idx : chosenIndices) {
            Map<String, Object> movie = availableMovies.get(idx);
            chosenMovies.add(movie);
            totalDuration += (Integer) movie.get("duration");
            totalScore += (Double) movie.get("score");
        }

        return Map.of(
            "movies", chosenMovies,
            "totalDuration", totalDuration,
            "totalScore", totalScore,
            "algorithm", "DP"
        );
    }

    /**
     * Find movie sequences using Backtracking.
     * Explores all valid sequences matching constraints.
     * 
     * Algorithm: Backtracking (Exhaustive Search)
     */
    public static Map<String, Object> findSequencesBacktracking(
        List<Map<String, Object>> availableMovies,
        int totalMinutes,
        double minRating
    ) {
        List<List<Map<String, Object>>> allSequences = new ArrayList<>();
        List<Map<String, Object>> currentSequence = new ArrayList<>();

        backtrack(
            availableMovies,
            0,
            totalMinutes,
            minRating,
            currentSequence,
            allSequences,
            0
        );

        if (allSequences.isEmpty()) {
            return Map.of(
                "sequences", List.of(),
                "count", 0,
                "algorithm", "Backtracking"
            );
        }

        List<Map<String, Object>> best = allSequences.stream()
            .max(Comparator.comparingInt(List::size))
            .orElse(List.of());

        return Map.of(
            "sequences", allSequences,
            "count", allSequences.size(),
            "bestSequence", best,
            "algorithm", "Backtracking"
        );
    }

    private static void backtrack(
        List<Map<String, Object>> movies,
        int start,
        int remainingTime,
        double minRating,
        List<Map<String, Object>> current,
        List<List<Map<String, Object>>> allSequences,
        int depth
    ) {
        if (depth > 10) {
            return;
        }

        if (!current.isEmpty()) {
            allSequences.add(new ArrayList<>(current));
        }

        for (int i = start; i < movies.size(); i++) {
            Map<String, Object> movie = movies.get(i);
            int duration = (Integer) movie.get("duration");
            double rating = (Double) movie.getOrDefault("rating", 0.0);

            if (duration <= remainingTime && rating >= minRating) {
                current.add(movie);
                backtrack(
                    movies,
                    i + 1,
                    remainingTime - duration,
                    minRating,
                    current,
                    allSequences,
                    depth + 1
                );
                current.remove(current.size() - 1);
            }
        }
    }

    /**
     * Optimize marathon using Branch & Bound.
     * Prunes suboptimal branches for efficiency.
     * 
     * Algorithm: Branch & Bound
     */
    public static Map<String, Object> optimizeMarathonBranchAndBound(
        List<Map<String, Object>> availableMovies,
        int totalMinutes,
        double minRating
    ) {
        if (availableMovies == null || availableMovies.isEmpty()) {
            return Map.of(
                "movies", List.of(),
                "totalDuration", 0,
                "totalScore", 0.0,
                "algorithm", "Branch & Bound"
            );
        }

        class State {
            int idx;
            int timeUsed;
            double scoreAchieved;
            List<Integer> chosen;

            State(int i, int t, double s, List<Integer> c) {
                idx = i;
                timeUsed = t;
                scoreAchieved = s;
                chosen = c;
            }
        }

        Comparator<State> cmp = Comparator.comparingDouble(
            s -> -s.scoreAchieved
        );
        PriorityQueue<State> pq = new PriorityQueue<>(cmp);
        pq.add(new State(0, 0, 0.0, new ArrayList<>()));

        double bestScore = 0.0;
        List<Integer> bestChosen = new ArrayList<>();

        while (!pq.isEmpty()) {
            State state = pq.poll();

            if (state.scoreAchieved > bestScore) {
                bestScore = state.scoreAchieved;
                bestChosen = state.chosen;
            }

            if (state.idx >= availableMovies.size()) {
                continue;
            }

            Map<String, Object> movie = availableMovies.get(state.idx);
            int duration = (Integer) movie.get("duration");
            double score = (Double) movie.get("score");
            double rating = (Double) movie.getOrDefault("rating", 0.0);

            if (state.timeUsed + duration <= totalMinutes &&
                rating >= minRating
            ) {
                List<Integer> newChosen = new ArrayList<>(state.chosen);
                newChosen.add(state.idx);
                pq.add(new State(
                    state.idx + 1,
                    state.timeUsed + duration,
                    state.scoreAchieved + score,
                    newChosen
                ));
            }

            pq.add(new State(
                state.idx + 1,
                state.timeUsed,
                state.scoreAchieved,
                new ArrayList<>(state.chosen)
            ));

            if (pq.size() > 10000) {
                break;
            }
        }

        List<Map<String, Object>> chosenMovies = new ArrayList<>();
        int totalDuration = 0;

        for (int idx : bestChosen) {
            Map<String, Object> movie = availableMovies.get(idx);
            chosenMovies.add(movie);
            totalDuration += (Integer) movie.get("duration");
        }

        return Map.of(
            "movies", chosenMovies,
            "totalDuration", totalDuration,
            "totalScore", bestScore,
            "algorithm", "Branch & Bound"
        );
    }

    /**
     * Calculate score for a movie based on rating and user preferences.
     */
    public static double calculateMovieScore(
        Map<String, Object> movie,
        List<String> preferredGenres
    ) {
        double rating = (Double) movie.getOrDefault("rating", 0.0);
        String genre = (String) movie.get("genre");

        double baseScore = rating * 20.0;

        if (preferredGenres != null &&
            !preferredGenres.isEmpty() &&
            genre != null &&
            preferredGenres.contains(genre)
        ) {
            baseScore *= 1.5;
        }

        return baseScore;
    }
}

