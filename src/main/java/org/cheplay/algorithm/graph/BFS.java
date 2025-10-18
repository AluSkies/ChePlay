package org.cheplay.algorithm.graph;

import java.util.*;

public class BFS {
    public static List<String> bfs(Map<String, Map<String, Double>> adj, String start) {
        if (start == null || !adj.containsKey(start)) return Collections.emptyList();
        List<String> order = new ArrayList<>();
        Queue<String> q = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        q.add(start);
        visited.add(start);
        while (!q.isEmpty()) {
            String u = q.poll();
            order.add(u);
            Map<String, Double> neighbors = adj.getOrDefault(u, Collections.emptyMap());
            for (String v : neighbors.keySet()) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    q.add(v);
                }
            }
        }
        return order;
    }
}
