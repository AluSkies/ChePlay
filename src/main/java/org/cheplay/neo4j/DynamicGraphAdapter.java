package org.cheplay.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cheplay.dto.AlgorithmRequest;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.stereotype.Service;

@Service
public class DynamicGraphAdapter {
    private final Driver driver;

    public DynamicGraphAdapter(Driver driver) {
        this.driver = driver;
    }

    /**
     * Build adjacency map from Neo4j based on the request.
     * Returns Map<from, Map<to, weight>>; ensures nodes exist even if they have no outgoing edges.
     */
    public Map<String, Map<String, Double>> buildAdjacency(AlgorithmRequest req) {
        String graphType = req.graphType != null ? req.graphType : "movies";
        double minScore = req.minScore != null ? req.minScore : 0.0;
        boolean undirected = req.undirected != null && req.undirected;
        List<String> platforms = req.platforms != null ? req.platforms : List.of();
        String onlyUserId = (req.onlyUserId != null && !req.onlyUserId.isBlank()) ? req.onlyUserId : null;

        Map<String, Map<String, Double>> adj = new HashMap<>();

        try (Session session = driver.session(SessionConfig.defaultConfig())) {
            List<org.neo4j.driver.Record> rows;
            if ("bands".equalsIgnoreCase(graphType)) {
                rows = runBandsQuery(session, minScore);
            } else if ("songs_hybrid".equalsIgnoreCase(graphType)) {
                rows = runSongsHybridQuery(session, req.params);
            } else {
                rows = runMoviesQuery(session, minScore, platforms, onlyUserId);
            }

            for (org.neo4j.driver.Record r : rows) {
                String from = r.get("from").asString();
                String to = r.get("to").asString();
                double weight = r.get("weight").asDouble();

                // ensure positive weights
                if (weight <= 0) continue;

                adj.computeIfAbsent(from, k -> new HashMap<>());
                adj.computeIfAbsent(to, k -> new HashMap<>());
                adj.get(from).put(to, Math.min(adj.get(from).getOrDefault(to, Double.POSITIVE_INFINITY), weight));
                if (undirected) {
                    adj.get(to).put(from, Math.min(adj.get(to).getOrDefault(from, Double.POSITIVE_INFINITY), weight));
                }
            }
        }

        // Also ensure the start node exists even if not present in query results
        if (req.start != null && !req.start.isBlank()) {
            adj.computeIfAbsent(req.start, k -> new HashMap<>());
        }

        return adj;
    }

    private List<org.neo4j.driver.Record> runSongsHybridQuery(Session session, Map<String, Object> params) {
     String cypher = """
         WITH toInteger($window) AS w, toFloat($lambda) AS lam
         MATCH (u:User)-[:LISTENED]->(s1:Song)
         WITH u, s1, w, lam
         MATCH (u)-[:LISTENED]->(s2:Song)
         WHERE s1 <> s2
         WITH s1, s2, count(DISTINCT u) AS overlap, w, lam
                 WHERE overlap > 0
                 WITH (CASE WHEN id(s1) < id(s2) THEN s1 ELSE s2 END) AS a,
             (CASE WHEN id(s1) < id(s2) THEN s2 ELSE s1 END) AS b,
             overlap,
             coalesce(w,10) AS win,
             coalesce(lam,0.5) AS lam
                 WITH a, b, overlap, lam, win,
                            CASE
                                    WHEN a.year IS NULL OR b.year IS NULL THEN 0.0
                                    ELSE abs(toFloat(a.year) - toFloat(b.year))
                            END AS d
                 WITH a, b,
                     (1.0 / (overlap + 1.0)) AS w1,
                     (CASE WHEN win = 0 THEN 0 ELSE (toFloat(d) / toFloat(win)) END) AS w2,
                     lam
         RETURN coalesce(a.id, a.name, a.title) AS from,
             coalesce(b.id, b.name, b.title) AS to,
             (w1 + lam * w2) AS weight
         """;

        Map<String, Object> map = new HashMap<>();
        if (params != null) {
            Object w = params.get("window");
            Object l = params.get("lambda");
            if (w != null) map.put("window", w);
            if (l != null) map.put("lambda", l);
        }
        // sensible defaults if not provided
        map.putIfAbsent("window", 10);
        map.putIfAbsent("lambda", 0.5);

        Result result = session.run(cypher, map);
        return result.list();
    }

    private List<org.neo4j.driver.Record> runMoviesQuery(Session session, double minScore, List<String> platforms, String onlyUserId) {
        String cypher = """
            MATCH (a:Movie)-[r:SIMILAR_TO]->(b:Movie)
            WHERE r.score >= $minScore
            OPTIONAL MATCH (b)-[:AVAILABLE_ON]->(s:StreamingService)
            WITH a, b, r, collect(DISTINCT s.name) AS svcs
            OPTIONAL MATCH (u:User {id: $onlyUserId})-[:SUBSCRIBED_TO]->(us:StreamingService)
            WITH a, b, r, svcs, collect(DISTINCT us.name) AS userSvcs, $platforms AS platforms, $onlyUserId AS onlyUserId
            WHERE (size(platforms) = 0 OR any(p IN platforms WHERE p IN svcs))
              AND (onlyUserId IS NULL OR any(p IN userSvcs WHERE p IN svcs))
            RETURN a.id AS from, b.id AS to, 1.0 / (r.score + 1.0) AS weight
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("minScore", minScore);
        params.put("platforms", platforms);
        params.put("onlyUserId", onlyUserId);
        Result result = session.run(cypher, params);
        return result.list();
    }

    private List<org.neo4j.driver.Record> runBandsQuery(Session session, double minScore) {
        String cypher = """
            MATCH (a:Band)-[r:SIMILAR_TO]->(b:Band)
            WHERE r.score >= $minScore
            RETURN a.name AS from, b.name AS to, 1.0 / (r.score + 1.0) AS weight
            """;
        Map<String, Object> params = Map.of("minScore", minScore);
        Result result = session.run(cypher, params);
        return result.list();
    }
}
