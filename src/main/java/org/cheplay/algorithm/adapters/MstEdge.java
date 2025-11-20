package org.cheplay.algorithm.adapters;

/**
 * Simple DTO for MST edges used by adapters.
 */
public final class MstEdge {
    private final String from;
    private final String to;
    private final double weight;

    public MstEdge(String from, String to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public double getWeight() { return weight; }
}
