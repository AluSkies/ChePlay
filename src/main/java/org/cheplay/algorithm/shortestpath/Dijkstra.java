package org.cheplay.algorithm.shortestpath;

import java.util.*;

public class Dijkstra {
    public static Map<String, Object> dijkstra(Map<String, Map<String, Double>> adj, String source) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String v : adj.keySet()) dist.put(v, Double.POSITIVE_INFINITY);
        if (source == null || !adj.containsKey(source)) {
            return Map.of("distances", dist, "prev", prev);
        }
        dist.put(source, 0.0);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(source);
        while (!pq.isEmpty()) {
            String u = pq.poll();
            for (Map.Entry<String, Double> e : adj.getOrDefault(u, Collections.emptyMap()).entrySet()) {
                String v = e.getKey();
                double w = e.getValue();
                double alt = dist.get(u) + w;
                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }
        return Map.of("distances", dist, "prev", prev);
    }
}
