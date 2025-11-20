package org.cheplay.algorithm.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DefaultDijkstraSolver implements ShortestPathSolver {

    @Override
    public Map<String, Double> multiSourceDijkstra(Map<String, Map<String, Double>> adj, List<String> sources) {
        Map<String, Double> dist = new HashMap<>();
        for (String v : adj.keySet()) dist.put(v, Double.POSITIVE_INFINITY);
        if (sources == null || sources.isEmpty()) return dist;

        java.util.PriorityQueue<String> pq = new java.util.PriorityQueue<>(java.util.Comparator.comparingDouble(dist::get));
        for (String s : sources) {
            if (!adj.containsKey(s)) continue;
            dist.put(s, 0.0);
            pq.add(s);
        }

        while (!pq.isEmpty()) {
            String u = pq.poll();
            for (Map.Entry<String, Double> e : adj.getOrDefault(u, java.util.Collections.emptyMap()).entrySet()) {
                String v = e.getKey();
                double w = e.getValue();
                double alt = dist.getOrDefault(u, Double.POSITIVE_INFINITY) + w;
                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }

        return dist;
    }
}
