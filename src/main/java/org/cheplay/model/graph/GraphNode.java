package org.cheplay.model.graph;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Node")
public class GraphNode {
    @Id
    private String id;
    private String label;

    // neighbors map: neighborId -> weight
    @Property("neighbors")
    private Map<String, Double> neighbors = new HashMap<>();

    public GraphNode() {}

    public GraphNode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Map<String, Double> getNeighbors() { return neighbors; }
    public void setNeighbors(Map<String, Double> neighbors) { this.neighbors = neighbors; }
}
