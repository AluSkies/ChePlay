package org.cheplay.model.recommendation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.cheplay.dto.AlgorithmRequest;
import org.springframework.stereotype.Service;

/**
 * Service that builds the songs_hybrid graph and recommends songs for a user
 * based on liked-song seeds. Uses DynamicGraphAdapter via AlgorithmRequest.
 */
@Service
public class SongRecommendationService {

    private final org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter;
    private final FriendRecommendationService friendService;

    public SongRecommendationService(org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter,
                                     FriendRecommendationService friendService) {
        this.dynamicGraphAdapter = Objects.requireNonNull(dynamicGraphAdapter);
        this.friendService = Objects.requireNonNull(friendService);
    }

    /**
     * Recommend songs for a user using the songs_hybrid graph.
     * Returns a list of maps with keys: id (song id) and score (lower is more similar/distance).
     */
    public List<Map<String, Object>> recommendForUser(String userId, int k, Integer window, Double lambda) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        int win = window != null ? window : 10;
        double lam = lambda != null ? lambda : 0.5;

        // get seeds (songs liked by the user)
        List<String> seeds = friendService.getUserLikedSongs(userId);
        if (seeds == null || seeds.isEmpty()) return List.of();

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "songs_hybrid";
        req.undirected = true;
        req.params = Map.of("window", win, "lambda", lam);

        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);

        // By default use multi-source Dijkstra (distance from any seed). This preserves
        // the semantics of "closest songs". We also provide a Prim-based option below
        // via a dedicated helper if needed (controller may call that variant).
        Map<String, Double> distances = runMultiSourceDijkstra(adj, seeds);

        Map<String, Double> best = new HashMap<>();
        for (Map.Entry<String, Double> e : distances.entrySet()) {
            String node = e.getKey();
            double d = e.getValue();
            if (Double.isInfinite(d)) continue;
            if (seeds.contains(node)) continue; // exclude seeds
            best.put(node, d);
        }

        return best.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(k <= 0 ? Integer.MAX_VALUE : k)
                .map(e -> Map.<String,Object>of("id", e.getKey(), "score", e.getValue()))
                .collect(Collectors.toList());
    }

    /** Run a multi-source Dijkstra over adj starting from all sources with distance 0. */
    private Map<String, Double> runMultiSourceDijkstra(Map<String, Map<String, Double>> adj, List<String> sources) {
        Map<String, Double> dist = new HashMap<>();
        for (String v : adj.keySet()) dist.put(v, Double.POSITIVE_INFINITY);
        if (sources == null || sources.isEmpty()) return dist;
        java.util.PriorityQueue<String> pq = new java.util.PriorityQueue<>(java.util.Comparator.comparingDouble(dist::get));
        for (String s : sources) {
            if (!adj.containsKey(s)) continue;
            dist.put(s, 0.0);
            pq.add(s);
        }
        while (!pq.isEmpty()) {
            String u = pq.poll();
            for (Map.Entry<String, Double> e : adj.getOrDefault(u, java.util.Collections.emptyMap()).entrySet()) {
                String v = e.getKey();
                double w = e.getValue();
                double alt = dist.getOrDefault(u, Double.POSITIVE_INFINITY) + w;
                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }
        return dist;
    }

    /** Build adjacency map of the MST returned by Prim and run multi-source Dijkstra on the tree. */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> recommendForUserUsingPrim(String userId, int k, Integer window, Double lambda) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        int win = window != null ? window : 10;
        double lam = lambda != null ? lambda : 0.5;

        List<String> seeds = friendService.getUserLikedSongs(userId);
        if (seeds == null || seeds.isEmpty()) return List.of();

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "songs_hybrid";
        req.undirected = true;
        req.params = Map.of("window", win, "lambda", lam);

        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);

        String start = seeds.stream().filter(adj::containsKey).findFirst().orElse(null);
        if (start == null) return List.of();
        Map<String, Object> mstRes = org.cheplay.algorithm.mst.Prim.minimumSpanningTree(adj, start);
        List<Object> edges = (List<Object>) mstRes.getOrDefault("mst", java.util.Collections.emptyList());
        Map<String, Map<String, Double>> treeAdj = new HashMap<>();
        for (Object o : edges) {
            if (o == null) continue;
            try {
                org.cheplay.algorithm.mst.Prim.Edge e = (org.cheplay.algorithm.mst.Prim.Edge) o;
                treeAdj.computeIfAbsent(e.from, x -> new HashMap<>()).put(e.to, e.weight);
                treeAdj.computeIfAbsent(e.to, x -> new HashMap<>()).put(e.from, e.weight);
            } catch (ClassCastException ex) {
                // ignore unexpected types
            }
        }

        Map<String, Double> distances = runMultiSourceDijkstra(treeAdj, seeds);
        Map<String, Double> best = new HashMap<>();
        for (Map.Entry<String, Double> e : distances.entrySet()) {
            String node = e.getKey();
            double d = e.getValue();
            if (Double.isInfinite(d)) continue;
            if (seeds.contains(node)) continue;
            best.put(node, d);
        }

        return best.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(k <= 0 ? Integer.MAX_VALUE : k)
                .map(e -> Map.<String,Object>of("id", e.getKey(), "score", e.getValue()))
                .collect(Collectors.toList());
    }
}
