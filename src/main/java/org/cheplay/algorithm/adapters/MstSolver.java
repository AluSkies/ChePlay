package org.cheplay.algorithm.adapters;

import java.util.Map;

/**
 * Abstraction for minimum-spanning-tree solvers.
 */
public interface MstSolver {
    /**
     * Build MST for the given adjacency map using the provided start node.
     * Returns a typed {@link MstResult} containing the MST edges and total weight.
     */
    MstResult minimumSpanningTree(Map<String, Map<String, Double>> adj, String start);
}
