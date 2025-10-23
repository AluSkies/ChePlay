package org.cheplay.dto;

import java.util.List;
import java.util.Map;

public class AlgorithmRequest {
    public List<String> nodes;
    public List<Map<String, Object>> edges;
    public String start;
    public String target;
    public List<Integer> numbers;
    public Map<String, Object> params;

    // New fields for dynamic Neo4j graph adapter
    // graphType: "movies" or "bands"
    public String graphType;
    // Minimum similarity score to consider
    public Double minScore;
    // If true, duplicate edges in both directions
    public Boolean undirected;
    // Platforms to filter (e.g., Netflix, Spotify)
    public List<String> platforms;
    // Optional: filter by a user and their subscribed services
    public String onlyUserId;
}
