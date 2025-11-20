package org.cheplay.algorithm.mst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.cheplay.algorithm.adapters.MstEdge;
import org.cheplay.algorithm.adapters.MstResult;

public class Prim {
    public static MstResult minimumSpanningTree(Map<String, Map<String, Double>> adj, String start) {
        if (start == null || !adj.containsKey(start)) return new MstResult(Collections.emptyList(), 0.0);
        Set<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weight));
        visited.add(start);
        for (Map.Entry<String, Double> e : adj.get(start).entrySet()) pq.add(new Edge(start, e.getKey(), e.getValue()));
        List<MstEdge> mst = new ArrayList<>();
        double total = 0;
        while (!pq.isEmpty()) {
            Edge e = pq.poll();
            if (visited.contains(e.to)) continue;
            visited.add(e.to);
            mst.add(new MstEdge(e.from, e.to, e.weight));
            total += e.weight;
            for (Map.Entry<String, Double> nb : adj.getOrDefault(e.to, Collections.emptyMap()).entrySet()) {
                if (!visited.contains(nb.getKey())) pq.add(new Edge(e.to, nb.getKey(), nb.getValue()));
            }
        }
        return new MstResult(mst, total);
    }

    public static class Edge {
        public String from, to;
        public double weight;
        public Edge(String f, String t, double w) { from = f; to = t; weight = w; }
        public String toString() { return from + "->" + to + " (" + weight + ")"; }
    }
}
