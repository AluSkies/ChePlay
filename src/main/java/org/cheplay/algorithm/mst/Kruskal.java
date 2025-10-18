package org.cheplay.algorithm.mst;

import java.util.*;

public class Kruskal {
    public static Map<String, Object> minimumSpanningTree(Map<String, Map<String, Double>> adj) {
        List<Edge> edges = new ArrayList<>();
        for (var u : adj.entrySet()) {
            for (var v : u.getValue().entrySet()) {
                edges.add(new Edge(u.getKey(), v.getKey(), v.getValue()));
            }
        }
        Set<String> seen = new HashSet<>();
        List<Edge> uniq = new ArrayList<>();
        for (Edge e : edges) {
            String key = e.from + "-" + e.to;
            String key2 = e.to + "-" + e.from;
            if (seen.contains(key) || seen.contains(key2)) continue;
            seen.add(key);
            uniq.add(e);
        }
        uniq.sort(Comparator.comparingDouble(o -> o.weight));
        UnionFind uf = new UnionFind();
        for (String v : adj.keySet()) uf.add(v);
        List<Edge> mst = new ArrayList<>();
        double total = 0;
        for (Edge e : uniq) {
            if (!uf.connected(e.from, e.to)) {
                uf.union(e.from, e.to);
                mst.add(e);
                total += e.weight;
            }
        }
        return Map.of("mst", mst, "weight", total);
    }

    public static class Edge {
        public String from, to;
        public double weight;
        public Edge(String f, String t, double w) { from = f; to = t; weight = w; }
        public String toString() { return from + "-" + to + " (" + weight + ")"; }
    }

    static class UnionFind {
        private final Map<String, String> parent = new HashMap<>();
        private final Map<String, Integer> rank = new HashMap<>();
        void add(String x) { parent.putIfAbsent(x, x); rank.putIfAbsent(x, 0); }
        String find(String x) {
            parent.putIfAbsent(x, x);
            if (!parent.get(x).equals(x)) parent.put(x, find(parent.get(x)));
            return parent.get(x);
        }
        boolean connected(String a, String b) { return find(a).equals(find(b)); }
        void union(String a, String b) {
            String ra = find(a), rb = find(b);
            if (ra.equals(rb)) return;
            int raRank = rank.getOrDefault(ra, 0);
            int rbRank = rank.getOrDefault(rb, 0);
            if (raRank < rbRank) parent.put(ra, rb);
            else if (raRank > rbRank) parent.put(rb, ra);
            else { parent.put(rb, ra); rank.put(ra, raRank+1); }
        }
    }
}
