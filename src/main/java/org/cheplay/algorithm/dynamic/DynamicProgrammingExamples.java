package org.cheplay.algorithm.dynamic;

import java.util.*;

public class DynamicProgrammingExamples {
    public static Map<String, Object> knapsackDP(Map<String, Object> params) {
        int capacity = (int) params.getOrDefault("capacity", 0);
        List<Integer> weights = (List<Integer>) params.getOrDefault("weights", Collections.emptyList());
        List<Integer> values = (List<Integer>) params.getOrDefault("values", Collections.emptyList());
        int n = weights.size();
        int[][] dp = new int[n + 1][capacity + 1];
        for (int i = 1; i <= n; i++) {
            int w = weights.get(i-1);
            int v = values.get(i-1);
            for (int c = 0; c <= capacity; c++) {
                dp[i][c] = dp[i-1][c];
                if (w <= c) dp[i][c] = Math.max(dp[i][c], dp[i-1][c-w] + v);
            }
        }
        List<Integer> chosen = new ArrayList<>();
        int c = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][c] != dp[i-1][c]) {
                chosen.add(i-1);
                c -= weights.get(i-1);
            }
        }
        Collections.reverse(chosen);
        return Map.of("maxValue", dp[n][capacity], "chosenIndices", chosen);
    }
}
