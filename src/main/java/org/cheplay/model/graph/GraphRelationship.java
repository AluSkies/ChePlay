package org.cheplay.model.graph;

public class GraphRelationship {
    private String id = java.util.UUID.randomUUID().toString();
    private String fromId;
    private String toId;
    private double weight = 1.0;

    public GraphRelationship() {}

    public GraphRelationship(String fromId, String toId, double weight) {
        this.fromId = fromId;
        this.toId = toId;
        this.weight = weight;
    }

    public String getId() { return id; }
    public String getFromId() { return fromId; }
    public String getToId() { return toId; }
    public double getWeight() { return weight; }

    public void setFromId(String fromId) { this.fromId = fromId; }
    public void setToId(String toId) { this.toId = toId; }
    public void setWeight(double weight) { this.weight = weight; }
}
