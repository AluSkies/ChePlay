package org.cheplay.model.recommendation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cheplay.algorithm.adapters.MstEdge;

/**
 * Small helpers shared by recommendation services to keep methods focused.
 */
public final class RecommendationHelpers {

    private RecommendationHelpers() {}

    public static Map<String, Map<String, Double>> buildAdjFromMst(List<MstEdge> edges) {
        Map<String, Map<String, Double>> treeAdj = new HashMap<>();
        if (edges == null) return treeAdj;
        for (MstEdge e : edges) {
            if (e == null) continue;
            treeAdj.computeIfAbsent(e.getFrom(), k -> new HashMap<>()).put(e.getTo(), e.getWeight());
            treeAdj.computeIfAbsent(e.getTo(), k -> new HashMap<>()).put(e.getFrom(), e.getWeight());
        }
        return treeAdj;
    }

    public static Map<String, Double> chooseBestOrFallback(Map<String, Double> distances, Set<String> exclude, Set<String> seedSet) {
        Map<String, Double> best = new HashMap<>();
        Map<String, Double> fallback = new HashMap<>();
        if (distances == null) return fallback;
        for (Map.Entry<String, Double> e : distances.entrySet()) {
            String node = e.getKey();
            double d = e.getValue();
            if (Double.isInfinite(d)) continue;
            if (seedSet != null && seedSet.contains(node)) continue;
            fallback.put(node, d);
            if (exclude == null || !exclude.contains(node)) {
                best.put(node, d);
            }
        }
        return best.isEmpty() ? fallback : best;
    }

    public static java.util.List<Map.Entry<String, Double>> rankDistances(Map<String, Double> distances, String sourceKey, int k) {
        if (distances == null) return java.util.List.of();
        int limit = k <= 0 ? Integer.MAX_VALUE : k;
        return distances.entrySet().stream()
                .filter(e -> !e.getKey().equals(sourceKey) && !Double.isInfinite(e.getValue()))
                .sorted(Map.Entry.comparingByValue())
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
}
