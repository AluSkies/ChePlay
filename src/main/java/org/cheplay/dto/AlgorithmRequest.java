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
}
