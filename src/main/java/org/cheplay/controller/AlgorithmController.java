package org.cheplay.controller;

import org.cheplay.dto.AlgorithmRequest;
import org.cheplay.dto.AlgorithmResponse;
import org.cheplay.service.AlgorithmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/algorithms")
public class AlgorithmController {
    private final AlgorithmService algorithmService;

    public AlgorithmController(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }

    @PostMapping("/bfs")
    public ResponseEntity<?> bfs(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "BFS";
        res.result = algorithmService.runBFS(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/dfs")
    public ResponseEntity<?> dfs(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "DFS";
        res.result = algorithmService.runDFS(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/dijkstra")
    public ResponseEntity<?> dijkstra(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "Dijkstra";
        res.result = algorithmService.runDijkstra(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/prim")
    public ResponseEntity<?> prim(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "Prim";
        res.result = algorithmService.runPrim(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/kruskal")
    public ResponseEntity<?> kruskal(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "Kruskal";
        res.result = algorithmService.runKruskal(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/quicksort")
    public ResponseEntity<?> quicksort(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "QuickSort";
        res.result = algorithmService.runQuickSort(req.numbers);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/mergesort")
    public ResponseEntity<?> mergesort(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "MergeSort";
        res.result = algorithmService.runMergeSort(req.numbers);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/greedy")
    public ResponseEntity<?> greedy(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "Greedy";
        res.result = algorithmService.runGreedy(req.params);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/dynamic")
    public ResponseEntity<?> dynamic(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "DynamicProgramming";
        res.result = algorithmService.runDynamic(req.params);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/backtracking")
    public ResponseEntity<?> backtracking(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "Backtracking";
        res.result = algorithmService.runBacktracking(req.params);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/branchandbound")
    public ResponseEntity<?> branchAndBound(@RequestBody AlgorithmRequest req) {
        AlgorithmResponse res = new AlgorithmResponse();
        res.algorithm = "BranchAndBound";
        res.result = algorithmService.runBranchAndBound(req.params);
        return ResponseEntity.ok(res);
    }
}
