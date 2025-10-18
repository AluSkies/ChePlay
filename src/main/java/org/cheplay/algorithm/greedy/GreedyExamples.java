package org.cheplay.algorithm.greedy;

import java.util.*;

public class GreedyExamples {
    public static Map<String, Object> coinChangeGreedy(Map<String, Object> params) {
        int amount = (int) params.getOrDefault("amount", 0);
        List<Integer> coins = (List<Integer>) params.getOrDefault("coins", Arrays.asList(25,10,5,1));
        coins = new ArrayList<>(coins);
        coins.sort(Comparator.reverseOrder());
        Map<Integer, Integer> used = new LinkedHashMap<>();
        for (int c : coins) {
            int cnt = amount / c;
            if (cnt > 0) {
                used.put(c, cnt);
                amount -= cnt * c;
            }
        }
        return Map.of("used", used, "remaining", amount);
    }
}
