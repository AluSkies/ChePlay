package org.cheplay.algorithm.adapters;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Typed result for MST computations. Keeps the list of edges and total weight.
 */
public final class MstResult {
    private final List<MstEdge> mst;
    private final double weight;

    public MstResult(List<MstEdge> mst, double weight) {
        this.mst = mst == null ? Collections.emptyList() : List.copyOf(mst);
        this.weight = weight;
    }

    public List<MstEdge> getMst() { return mst; }
    public double getWeight() { return weight; }

    /**
     * Backwards-compatible map representation for code that expects a Map.
     */
    public Map<String, Object> toMap() {
        return Map.of("mst", mst, "weight", weight);
    }
}
