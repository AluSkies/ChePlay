package org.cheplay.model.recommendation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.cheplay.dto.AlgorithmRequest;
import org.cheplay.neo4j.DbConnector;
import org.springframework.stereotype.Service;

/**
 * Service that builds the songs_hybrid graph and recommends songs for a user
 * based on LISTENED seeds. Uses DynamicGraphAdapter via AlgorithmRequest.
 */
@Service
public class SongRecommendationService {

    private final org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter;
    private final DbConnector db;
    private final SongRecommendationMapper mapper;

    public SongRecommendationService(org.cheplay.neo4j.DynamicGraphAdapter dynamicGraphAdapter,
                                     DbConnector db,
                                     SongRecommendationMapper mapper) {
        this.dynamicGraphAdapter = Objects.requireNonNull(dynamicGraphAdapter);
        this.db = Objects.requireNonNull(db);
        this.mapper = Objects.requireNonNull(mapper);
    }

    private static final int MAX_SEED_COUNT = 15;

    /**
     * Recommend songs for a user using the songs_hybrid graph.
     * Returns a list of maps with keys: id (song id) and score (lower is more similar/distance).
     */
    public List<Map<String, Object>> recommendForUser(String userId, int k, Integer window, Double lambda) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        int win = window != null ? window : 10;
        double lam = lambda != null ? lambda : 0.5;

        List<String> listened = getUserListenedSongs(userId);
        if (listened == null || listened.isEmpty()) return List.of();

        List<String> seeds = selectSeedSongs(userId, listened);
        if (seeds.isEmpty()) return List.of();

        Set<String> exclude = new HashSet<>(listened);
        Set<String> seedSet = new HashSet<>(seeds);

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "songs_hybrid";
        req.undirected = true;
        req.params = Map.of("window", win, "lambda", lam);

        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);

        
        Map<String, Double> distances = runMultiSourceDijkstra(adj, seeds);

        Map<String, Double> best = new HashMap<>();
        Map<String, Double> fallback = new HashMap<>();
        for (Map.Entry<String, Double> e : distances.entrySet()) {
            String node = e.getKey();
            double d = e.getValue();
            if (Double.isInfinite(d)) continue;
            if (seedSet.contains(node)) continue; // never recommend exact seeds
            fallback.put(node, d);
            if (!exclude.contains(node)) {
                best.put(node, d);
            }
        }

        Map<String, Double> chosen = best.isEmpty() ? fallback : best;
        return mapper.toRecommendationList(chosen, k);
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

        List<String> listened = getUserListenedSongs(userId);
        if (listened == null || listened.isEmpty()) return List.of();

        List<String> seeds = selectSeedSongs(userId, listened);
        if (seeds.isEmpty()) return List.of();

    Set<String> exclude = new HashSet<>(listened);
    Set<String> seedSet = new HashSet<>(seeds);

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
        Map<String, Double> fallback = new HashMap<>();
        for (Map.Entry<String, Double> e : distances.entrySet()) {
            String node = e.getKey();
            double d = e.getValue();
            if (Double.isInfinite(d)) continue;
            if (seedSet.contains(node)) continue;
            fallback.put(node, d);
            if (!exclude.contains(node)) {
                best.put(node, d);
            }
        }

        Map<String, Double> chosen = best.isEmpty() ? fallback : best;
        return mapper.toRecommendationList(chosen, k);
    }

    private List<String> selectSeedSongs(String userId, List<String> fallback) {
        List<String> topSeeds = getUserTopListenedSeeds(userId, MAX_SEED_COUNT);
        List<String> seeds = (topSeeds == null || topSeeds.isEmpty()) ? fallback : topSeeds;
        if (seeds == null) {
            seeds = List.of();
        }
        return seeds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(MAX_SEED_COUNT)
                .collect(Collectors.toList());
    }

    private List<String> getUserListenedSongs(String userId) {
        String cypher = """
            MATCH (p:User)
            WHERE toLower(coalesce(p.id,p.nombre,p.name)) = toLower($userId)
            MATCH (p)-[:LISTENED]->(s:Song)
            RETURN DISTINCT coalesce(s.id,s.name,s.title) AS song
            ORDER BY song
            """;

        Map<String, Object> params = Map.of("userId", userId);

        return db.readList(cypher, params, record -> {
            if (record == null || record.get("song").isNull()) {
                return null;
            }
            String value = record.get("song").asString("").trim();
            return value.isEmpty() ? null : value;
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> getUserTopListenedSeeds(String userId, int limit) {
        int capped = limit <= 0 ? MAX_SEED_COUNT : Math.min(limit, 2 * MAX_SEED_COUNT);

        String cypher = """
            MATCH (p:User)
            WHERE toLower(coalesce(p.id,p.nombre,p.name)) = toLower($userId)
            MATCH (p)-[l:LISTENED]->(s:Song)
            RETURN coalesce(s.id,s.name,s.title) AS song,
                   coalesce(l.count, 0) AS plays
            ORDER BY plays DESC, song ASC
            LIMIT $limit
            """;

        Map<String, Object> params = Map.of("userId", userId, "limit", capped);

        return db.readList(cypher, params, record -> {
            if (record == null || record.get("song").isNull()) {
                return null;
            }
            String value = record.get("song").asString("").trim();
            return value.isEmpty() ? null : value;
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Map<String, Object> debugListenedData(String userId, int limit) {
        int cappedLimit = limit <= 0 ? 20 : Math.min(limit, 200);

    Map<String, Object> params = Map.of("userId", userId, "limit", cappedLimit);

    List<String> listenedAll = getUserListenedSongs(userId);
    List<String> seedSample = selectSeedSongs(userId, listenedAll);

        String listensCypher = """
            MATCH (p:User)
            WHERE toLower(coalesce(p.id,p.nombre,p.name)) = toLower($userId)
            MATCH (p)-[l:LISTENED]->(s:Song)
         RETURN coalesce(s.id,s.name,s.title) AS song,
             coalesce(s.title,s.name,s.id) AS title,
             coalesce(s.artist, s.band, '') AS artist,
             l.count AS plays
         ORDER BY coalesce(plays, 0) DESC, song ASC
            LIMIT $limit
            """;

        List<Map<String, Object>> listened = db.readList(listensCypher, params, record -> {
            if (record == null || record.get("song").isNull()) {
                return null;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("song", record.get("song").asString(""));
            row.put("title", record.get("title").asString(""));
            if (!record.get("artist").isNull()) {
                row.put("artist", record.get("artist").asString(""));
            }
            if (!record.get("plays").isNull()) {
                row.put("plays", record.get("plays").asInt(0));
            }
            return row;
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());

        String edgesCypher = """
            MATCH (u:User)-[:LISTENED]->(s1:Song)
            WITH u, s1
            MATCH (u)-[:LISTENED]->(s2:Song)
            WHERE s1 <> s2
            WITH coalesce(s1.id,s1.name,s1.title) AS from,
                 coalesce(s2.id,s2.name,s2.title) AS to,
                 count(DISTINCT u) AS overlap
            WHERE from IS NOT NULL AND to IS NOT NULL
            RETURN from, to, overlap
            ORDER BY overlap DESC, from ASC, to ASC
            LIMIT $limit
            """;

        List<Map<String, Object>> sampleEdges = db.readList(edgesCypher, Map.of("limit", cappedLimit), record -> {
            if (record == null || record.get("from").isNull() || record.get("to").isNull()) {
                return null;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("from", record.get("from").asString(""));
            row.put("to", record.get("to").asString(""));
            row.put("overlap", record.get("overlap").asInt(0));
            return row;
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());

        AlgorithmRequest req = new AlgorithmRequest();
        req.graphType = "songs_hybrid";
        req.undirected = true;
        req.params = Map.of("window", 10, "lambda", 0.5);
        Map<String, Map<String, Double>> adj = dynamicGraphAdapter.buildAdjacency(req);
        int nodeCount = adj.size();
        int edgeCount = adj.values().stream().mapToInt(Map::size).sum();

        Set<String> candidates = new HashSet<>(adj.keySet());
        if (listenedAll != null) {
            candidates.removeAll(listenedAll);
        }

        Map<String, Object> out = new HashMap<>();
        out.put("user", userId);
        out.put("seedSample", seedSample);
        out.put("listenedSeeds", listenedAll);
        out.put("listenedSample", listened);
        out.put("graphSample", sampleEdges);
        out.put("seedCount", listenedAll != null ? listenedAll.size() : 0);
        out.put("graphNodes", nodeCount);
        out.put("graphEdges", edgeCount);
        out.put("candidateCount", candidates.size());
        out.put("candidateSample", candidates.stream().limit(20).collect(Collectors.toList()));
        return out;
    }

}
