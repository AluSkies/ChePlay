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
        List<Integer> copy = new ArrayList<>(numbers);
        QuickSort.quicksort(copy, 0, copy.size() - 1);
        return copy;
    }

    public Object runMergeSort(List<Integer> numbers) {
        if (numbers == null) return Collections.emptyList();
        List<Integer> copy = new ArrayList<>(numbers);
        MergeSort.mergeSort(copy, 0, copy.size() - 1);
        return copy;
    }

    public Object runGreedy(Map<String, Object> params) {
        return GreedyExamples.coinChangeGreedy(params);
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
