package org.cheplay.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.cheplay.algorithm.backtracking.BacktrackingExamples;
import org.cheplay.algorithm.branchandbound.BranchAndBoundExamples;
import org.cheplay.algorithm.divideandconquer.MergeSort;
import org.cheplay.algorithm.divideandconquer.QuickSort;
import org.cheplay.algorithm.dynamic.DynamicProgrammingExamples;
import org.cheplay.algorithm.graph.BFS;
import org.cheplay.algorithm.graph.DFS;
import org.cheplay.algorithm.greedy.GreedyExamples;
import org.cheplay.algorithm.mst.Kruskal;
import org.cheplay.algorithm.mst.Prim;
import org.cheplay.algorithm.shortestpath.Dijkstra;
import org.cheplay.dto.AlgorithmRequest;
import org.cheplay.neo4j.DynamicGraphAdapter;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmService {
    private final DynamicGraphAdapter dynamicGraphAdapter;

    public AlgorithmService(DynamicGraphAdapter dynamicGraphAdapter) {
        this.dynamicGraphAdapter = dynamicGraphAdapter;
    }

    public Object runBFS(AlgorithmRequest req) {
        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);
        List<String> order = BFS.bfs(adj, req.start);
        return Map.of("order", order);
    }

    public Object runDFS(AlgorithmRequest req) {
        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);
        List<String> order = new ArrayList<>();
        DFS.dfs(req.start, adj, new HashSet<>(), order);
        return Map.of("order", order);
    }

    public Object runDijkstra(AlgorithmRequest req) {
        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);
        return Dijkstra.dijkstra(adj, req.start);
    }

    public Object runPrim(AlgorithmRequest req) {
        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);
        return Prim.minimumSpanningTree(adj, req.start);
    }

    public Object runKruskal(AlgorithmRequest req) {
        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);
        return Kruskal.minimumSpanningTree(adj);
    }

    public Object runQuickSort(List<Integer> numbers) {
        if (numbers == null) return Collections.emptyList();
        // Adapt to new QuickSort signature: expects LinkedHashMap<String,Integer>, sorts by value DESC
        java.util.LinkedHashMap<String, Integer> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < numbers.size(); i++) map.put(String.valueOf(i), numbers.get(i));
        java.util.LinkedHashMap<String, Integer> sorted = QuickSort.quicksort(map);
        return new ArrayList<>(sorted.values());
    }

    public Object runMergeSort(List<Integer> numbers) {
        if (numbers == null) return Collections.emptyList();
        // Adapt to new MergeSort signature: mergeSortByValue(Map<String,Integer>) returns LinkedHashMap sorted by value DESC
        java.util.LinkedHashMap<String, Integer> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < numbers.size(); i++) map.put(String.valueOf(i), numbers.get(i));
        java.util.LinkedHashMap<String, Integer> sorted = MergeSort.mergeSortByValue(map);
        return new ArrayList<>(sorted.values());
    }

    public Object runGreedy(Map<String, Object> params) {
        // Expecting params: { songs: Map<String,Integer>, k: int }
        if (params == null) return List.of();
        @SuppressWarnings("unchecked")
        Map<String, Integer> songs = (Map<String, Integer>) params.get("songs");
        Object kObj = params.get("k");
        int k = (kObj instanceof Number) ? ((Number) kObj).intValue() : 0;
        if (songs == null || songs.isEmpty() || k <= 0) return List.of();
        List<Map.Entry<String, Integer>> top = GreedyExamples.topKGreedy(songs, k);
        // Return as a lightweight list of maps for clean JSON
        List<Map<String, Object>> out = new ArrayList<>(top.size());
        for (Map.Entry<String, Integer> e : top) out.add(Map.of("id", e.getKey(), "plays", e.getValue()));
        return out;
    }

    public Object runDynamic(Map<String, Object> params) {
        return DynamicProgrammingExamples.knapsackDP(params);
    }

    public Object runBacktracking(Map<String, Object> params) {
        return BacktrackingExamples.solveNQueens(params);
    }

    public Object runBranchAndBound(Map<String, Object> params) {
        return BranchAndBoundExamples.simpleSubsetSumBB(params);
    }
}
