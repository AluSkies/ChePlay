package org.cheplay.service;

import org.cheplay.algorithm.divideandconquer.MergeSort;
import org.cheplay.algorithm.divideandconquer.QuickSort;
import org.cheplay.algorithm.graph.BFS;
import org.cheplay.algorithm.graph.DFS;
import org.cheplay.algorithm.mst.Kruskal;
import org.cheplay.algorithm.mst.Prim;
import org.cheplay.algorithm.shortestpath.Dijkstra;
import org.cheplay.algorithm.greedy.GreedyExamples;
import org.cheplay.algorithm.dynamic.DynamicProgrammingExamples;
import org.cheplay.algorithm.backtracking.BacktrackingExamples;
import org.cheplay.algorithm.branchandbound.BranchAndBoundExamples;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AlgorithmService {
    private final GraphService graphService;

    public AlgorithmService(GraphService graphService) {
        this.graphService = graphService;
    }

    public Object runBFS(String start) {
        Map<String, Map<String, Double>> adj = graphService.buildAdjacencyMap();
        return BFS.bfs(adj, start);
    }

    public Object runDFS(String start) {
        Map<String, Map<String, Double>> adj = graphService.buildAdjacencyMap();
        List<String> order = new ArrayList<>();
        DFS.dfs(start, adj, new HashSet<>(), order);
        return order;
    }

    public Object runDijkstra(String source) {
        Map<String, Map<String, Double>> adj = graphService.buildAdjacencyMap();
        return Dijkstra.dijkstra(adj, source);
    }

    public Object runPrim(String start) {
        Map<String, Map<String, Double>> adj = graphService.buildAdjacencyMap();
        return Prim.minimumSpanningTree(adj, start);
    }

    public Object runKruskal() {
        Map<String, Map<String, Double>> adj = graphService.buildAdjacencyMap();
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
