package org.cheplay.algorithm.mst;

import java.util.*;

public class Prim {
    public static Map<String, Object> minimumSpanningTree(Map<String, Map<String, Double>> adj, String start) {
        if (start == null || !adj.containsKey(start)) return Map.of("mst", Collections.emptyList(), "weight", 0.0);
        Set<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weight));
        visited.add(start);
        for (Map.Entry<String, Double> e : adj.get(start).entrySet()) pq.add(new Edge(start, e.getKey(), e.getValue()));
        List<Edge> mst = new ArrayList<>();
        double total = 0;
        while (!pq.isEmpty()) {
            Edge e = pq.poll();
            if (visited.contains(e.to)) continue;
            visited.add(e.to);
            mst.add(e);
            total += e.weight;
            for (Map.Entry<String, Double> nb : adj.getOrDefault(e.to, Collections.emptyMap()).entrySet()) {
                if (!visited.contains(nb.getKey())) pq.add(new Edge(e.to, nb.getKey(), nb.getValue()));
            }
        }
        return Map.of("mst", mst, "weight", total);
    }

    public static class Edge {
        public String from, to;
        public double weight;
        public Edge(String f, String t, double w) { from = f; to = t; weight = w; }
        public String toString() { return from + "->" + to + " (" + weight + ")"; }
    }
}
