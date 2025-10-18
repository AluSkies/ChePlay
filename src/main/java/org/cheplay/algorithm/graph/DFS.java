package org.cheplay.algorithm.graph;

import java.util.*;

public class DFS {
    public static void dfs(String node,
                           Map<String, Map<String, Double>> adj,
                           Set<String> visited,
                           List<String> order) {
        if (node == null || visited.contains(node)) return;
        visited.add(node);
        order.add(node);
        for (String nb : adj.getOrDefault(node, Collections.emptyMap()).keySet()) {
            if (!visited.contains(nb)) dfs(nb, adj, visited, order);
        }
    }
}
