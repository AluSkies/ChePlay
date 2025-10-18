package org.cheplay.service;

import org.cheplay.model.GraphNode;
import org.cheplay.repository.GraphNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GraphService {
    private final GraphNodeRepository nodeRepo;

    public GraphService(GraphNodeRepository nodeRepo) {
        this.nodeRepo = nodeRepo;
    }

    @Transactional
    public GraphNode createOrUpdateNode(GraphNode node) {
        return nodeRepo.save(node);
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Double>> buildAdjacencyMap() {
        Map<String, Map<String, Double>> adj = new HashMap<>();
        Iterable<GraphNode> nodes = nodeRepo.findAll();
        for (GraphNode n : nodes) {
            adj.putIfAbsent(n.getId(), new HashMap<>());
            if (n.getNeighbors() != null) {
                adj.get(n.getId()).putAll(n.getNeighbors());
            }
        }
        return adj;
    }

    @Transactional
    public void clearAll() {
        nodeRepo.deleteAll();
    }
}
