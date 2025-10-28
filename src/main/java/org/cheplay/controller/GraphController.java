package org.cheplay.controller;

import java.util.HashMap;
import java.util.Map;

import org.cheplay.model.graph.GraphNode;
import org.cheplay.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
public class GraphController {
    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clear() {
        graphService.clearAll();
        return ResponseEntity.ok("Cleared");
    }

    @PostMapping("/sample")
    public ResponseEntity<?> createSample() {
        GraphNode a = new GraphNode("A", "A");
        GraphNode b = new GraphNode("B", "B");
        GraphNode c = new GraphNode("C", "C");
        GraphNode d = new GraphNode("D", "D");

        Map<String, Double> na = new HashMap<>();
        na.put("B", 1.0);
        na.put("C", 4.0);
        a.setNeighbors(na);

        Map<String, Double> nb = new HashMap<>();
        nb.put("A", 1.0);
        nb.put("C", 2.0);
        b.setNeighbors(nb);

        Map<String, Double> nc = new HashMap<>();
        nc.put("A", 4.0);
        nc.put("B", 2.0);
        nc.put("D", 1.0);
        c.setNeighbors(nc);

        Map<String, Double> nd = new HashMap<>();
        nd.put("C", 1.0);
        d.setNeighbors(nd);

        graphService.createOrUpdateNode(a);
        graphService.createOrUpdateNode(b);
        graphService.createOrUpdateNode(c);
        graphService.createOrUpdateNode(d);

        return ResponseEntity.ok("Sample graph created");
    }
}
