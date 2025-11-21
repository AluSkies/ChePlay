package org.cheplay.algorithm.adapters;

import java.util.Map;

import org.cheplay.algorithm.mst.Prim;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around the existing Prim implementation to allow injection.
 */
@Component
public class PrimMstSolver implements MstSolver {

    @Override
    public MstResult minimumSpanningTree(Map<String, Map<String, Double>> adj, String start) {
        return Prim.minimumSpanningTree(adj, start);
    }
}
