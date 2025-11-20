package org.cheplay.algorithm.adapters;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for shortest-path computations used by services.
 */
public interface ShortestPathSolver {
    /**
     * Run a multi-source shortest-path (Dijkstra) over the adjacency map.
     * Returns a map node -> distance (Double.POSITIVE_INFINITY for unreachable).
     */
    Map<String, Double> multiSourceDijkstra(Map<String, Map<String, Double>> adj, List<String> sources);
}
