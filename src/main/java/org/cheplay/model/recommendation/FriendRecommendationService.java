package org.cheplay.model.recommendation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.cheplay.algorithm.shortestpath.Dijkstra;
import org.cheplay.model.graph.GraphRelationship;
import org.cheplay.neo4j.DbConnector;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Service;

/**
 * Servicio para recomendar "amigos" (usuarios) basados en canciones/movies en común.
 *
 * - Recomendaciones directas: consulta parametrizada por userId para devolver vecinos directos
 *   y sus pesos (1.0 / (overlap + 1.0)).
 * - Recomendaciones por camino: construye grafo completo (consulta global) y aplica Dijkstra
 *   para ordenar usuarios por distancia acumulada.
 */
@Service
public class FriendRecommendationService {

    private final DbConnector db;
        private final FriendRecommendationMapper mapper;

        public FriendRecommendationService(DbConnector db, FriendRecommendationMapper mapper) {
        this.db = Objects.requireNonNull(db, "DbConnector");
                this.mapper = Objects.requireNonNull(mapper);
    }

    // Consulta global (puede usarse para construir el grafo completo)
    private static final String GLOBAL_CYPHER =
            "MATCH (p1:User)-[:LIKED_SONG]->(s:Song)<-[:LIKED_SONG]-(p2:User) " +
            "WITH p1, p2, count(DISTINCT s) AS overlap " +
            "WHERE overlap > 0 " +
            "RETURN coalesce(p1.id, p1.nombre, p1.name) AS from, coalesce(p2.id, p2.nombre, p2.name) AS to, 1.0 / (overlap + 1.0) AS weight";

    // Consulta parametrizada para vecinos directos de un usuario
    private static final String USER_NEIGHBORS_CYPHER =
            "MATCH (p1:User) WHERE toLower(coalesce(p1.id, p1.nombre, p1.name)) = toLower($userId) " +
            "MATCH (p1)-[:LIKED_SONG]->(s:Song)<-[:LIKED_SONG]-(p2:User) " +
            "WITH coalesce(p2.id, p2.nombre, p2.name) AS neighbor, count(DISTINCT s) AS overlap " +
            "WHERE overlap > 0 " +
            "RETURN neighbor, 1.0 / (overlap + 1.0) AS weight " +
            "ORDER BY overlap DESC";

    /**
     * Construye el grafo completo (adjacency map) usando la consulta global.
     * Retorna Map<nodo, Map<vecino, peso>> (no dirigido: ambas direcciones cargadas).
     */
    public Map<String, Map<String, Double>> buildFullAdjacencyGraph() {
        List<GraphRelationship> edges = db.readList(GLOBAL_CYPHER, null, (Record r) ->
                new GraphRelationship(
                        r.get("from").asString(),
                        r.get("to").asString(),
                        r.get("weight").asDouble()
                )
        );

        Map<String, Map<String, Double>> adj = new HashMap<>();
        for (GraphRelationship e : edges) {
            adj.computeIfAbsent(e.getFromId(), k -> new HashMap<>()).put(e.getToId(), e.getWeight());
            adj.computeIfAbsent(e.getToId(), k -> new HashMap<>()).put(e.getFromId(), e.getWeight());
        }
        return adj;
    }

    /**
     * Devuelve los vecinos directos recomendados para un usuario (top-k) ordenados por peso ascendente
     * (peso = 1/(overlap+1) => menor peso = mayor overlap).
     */
    public List<Map<String, Object>> recommendDirectNeighbors(String userId, int k) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        Map<String, Object> params = Map.of("userId", userId);
        List<Map.Entry<String, Double>> list = db.readList(USER_NEIGHBORS_CYPHER, params, (Record r) ->
                Map.entry(r.get("neighbor").asString(), r.get("weight").asDouble())
        );

        List<Map.Entry<String, Double>> ranked = list.stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .limit(k <= 0 ? Integer.MAX_VALUE : k)
                .collect(Collectors.toList());

        return mapper.decorateRankedUsers(ranked, "weight");
    }

    /**
     * Recomendación por distancia acumulada (Dijkstra) usando el grafo completo. Devuelve top-k usuarios
     * con menor distancia (excluye al propio userId).
     */
    public List<Map<String, Object>> recommendByShortestPath(String userId, int k) {
                if (userId == null) throw new IllegalArgumentException("userId is required");
                Map<String, Map<String, Double>> adj = buildFullAdjacencyGraph();

                // Resolve actual key in adjacency map (case-insensitive fallback)
                String sourceKey = resolveKey(adj, userId);
                if (sourceKey == null) return List.of();

                Map<String, Object> res = Dijkstra.dijkstra(adj, sourceKey);
                @SuppressWarnings("unchecked")
                Map<String, Double> distances = (Map<String, Double>) res.get("distances");

                List<Map.Entry<String, Double>> ranked = distances.entrySet().stream()
                                .filter(e -> !e.getKey().equals(sourceKey) && !Double.isInfinite(e.getValue()))
                                .sorted(Map.Entry.comparingByValue())
                                .limit(k <= 0 ? Integer.MAX_VALUE : k)
                                .collect(Collectors.toList());

                return mapper.decorateRankedUsers(ranked, "distance");
    }

        /**
         * Encuentra la persona más cercana al userId usando Dijkstra y devolviendo el único mejor candidato.
         * Retorna null si no hay ningún vecino alcanzable.
         */
        public Map<String, Object> findClosestUser(String userId) {
                if (userId == null) throw new IllegalArgumentException("userId is required");
                Map<String, Map<String, Double>> adj = buildFullAdjacencyGraph();
                String sourceKey = resolveKey(adj, userId);
                if (sourceKey == null) return null;

                Map<String, Object> res = Dijkstra.dijkstra(adj, sourceKey);
                @SuppressWarnings("unchecked")
                Map<String, Double> distances = (Map<String, Double>) res.get("distances");

                Map.Entry<String, Double> best = distances.entrySet().stream()
                                .filter(e -> !e.getKey().equals(sourceKey) && !Double.isInfinite(e.getValue()))
                                .min(Map.Entry.comparingByValue())
                                .orElse(null);

                if (best == null) return null;
                return mapper.decorateRankedUsers(List.of(best), "distance").stream().findFirst().orElse(null);
        }

        // Helper: try exact match, then case-insensitive match of keys in adjacency map
        private String resolveKey(Map<String, Map<String, Double>> adj, String userId) {
                if (adj.containsKey(userId)) return userId;
                String lower = userId.toLowerCase();
                for (String k : adj.keySet()) {
                        if (k != null && k.toLowerCase().equals(lower)) return k;
                }
                return null;
        }

    /**
     * Lista las claves de usuario que el servicio usa como identificador (coalesce de id/nombre/name).
     * Útil para saber qué valor pasar como `userId` a las funciones de recomendación.
     */
        public List<String> listUserKeys() {
                // Only return non-null coalesced keys to avoid null values in the result
                String cypher = "MATCH (p:User) WHERE coalesce(p.id, p.nombre, p.name) IS NOT NULL " +
                                "RETURN DISTINCT coalesce(p.id, p.nombre, p.name) AS key ORDER BY key";
                return db.readList(cypher, null, (Record r) -> r.get("key").asString());
        }

        public List<Map<String, Object>> listUsersDecorated() {
                List<String> keys = listUserKeys();
                return mapper.decorateUsers(keys);
        }

            /**
             * Intenta resolver la clave exacta del usuario consultando la BD (case-insensitive).
             * Retorna null si no existe.
             */
            public String findUserKeyInDb(String userId) {
                if (userId == null) return null;
                String cypher = "MATCH (p:User) WHERE toLower(coalesce(p.id,p.nombre,p.name)) = toLower($userId) " +
                        "RETURN coalesce(p.id,p.nombre,p.name) AS key LIMIT 1";
                List<String> res = db.readList(cypher, Map.of("userId", userId), (Record r) -> r.get("key").asString());
                return res.isEmpty() ? null : res.get(0);
            }

            /**
             * Lista las canciones que el usuario ha marcado como LIKED_SONG (coalesce para id/title/name).
             */
            public List<String> getUserLikedSongs(String userId) {
                String cypher = "MATCH (p:User) WHERE toLower(coalesce(p.id,p.nombre,p.name)) = toLower($userId) " +
                        "MATCH (p)-[:LIKED_SONG]->(s:Song) RETURN DISTINCT coalesce(s.id,s.name,s.title) AS song ORDER BY song";
                return db.readList(cypher, Map.of("userId", userId), (Record r) -> r.get("song").asString());
            }

            /**
             * Devuelve vecinos y el overlap (cantidad de canciones en común) para un usuario dado.
             */
            public List<Map<String, Object>> getNeighborsWithOverlap(String userId) {
                String cypher = "MATCH (p1:User) WHERE toLower(coalesce(p1.id,p1.nombre,p1.name)) = toLower($userId) " +
                        "MATCH (p1)-[:LIKED_SONG]->(s:Song)<-[:LIKED_SONG]-(p2:User) " +
                        "WITH coalesce(p2.id,p2.nombre,p2.name) AS neighbor, count(DISTINCT s) AS overlap " +
                        "WHERE overlap > 0 RETURN neighbor, overlap, 1.0/(overlap+1.0) AS weight ORDER BY overlap DESC";
                return db.readList(cypher, Map.of("userId", userId), (Record r) -> Map.of(
                        "neighbor", r.get("neighbor").asString(),
                        "overlap", r.get("overlap").asLong(),
                        "weight", r.get("weight").asDouble()
                ));
            }

                /**
                 * Devuelve vecinos junto con las canciones compartidas (lista de ids/nombres de song) y el overlap.
                 * Cada elemento es un Map con keys: neighbor (String), overlap (long), weight (double), songs (List<String>)
                 */
                public List<Map<String, Object>> getNeighborsWithSharedSongs(String userId) {
                        String cypher = "MATCH (p1:User) WHERE toLower(coalesce(p1.id,p1.nombre,p1.name)) = toLower($userId) " +
                                        "MATCH (p1)-[:LIKED_SONG]->(s:Song)<-[:LIKED_SONG]-(p2:User) " +
                                        "WITH coalesce(p2.id,p2.nombre,p2.name) AS neighbor, collect(DISTINCT coalesce(s.id,s.name,s.title)) AS songs, count(DISTINCT s) AS overlap " +
                                        "WHERE overlap > 0 RETURN neighbor, overlap, 1.0/(overlap+1.0) AS weight, songs ORDER BY overlap DESC";

                        return db.readList(cypher, Map.of("userId", userId), (Record r) -> {
                                List<String> songs = r.get("songs").asList(v -> v.asString());
                                return Map.of(
                                                "neighbor", r.get("neighbor").asString(),
                                                "overlap", r.get("overlap").asLong(),
                                                "weight", r.get("weight").asDouble(),
                                                "songs", songs
                                );
                        });
                }

                public Map<String, Object> debugUserData(String userId, int limit) {
                        int cappedLimit = limit <= 0 ? 20 : Math.min(limit, 200);

                        Map<String, Object> out = new HashMap<>();
                        out.put("input", userId);

                        String resolved = findUserKeyInDb(userId);
                        out.put("resolvedKey", resolved);

                        Map<String, Object> userNode = fetchUserNodeDetails(resolved != null ? resolved : userId);
                        out.put("userNode", userNode);

                        List<String> likedSongs = getUserLikedSongs(userId);
                        out.put("likedSongs", likedSongs);

                        List<Map<String, Object>> neighbors = getNeighborsWithOverlap(userId);
                        out.put("neighborsCount", neighbors.size());
                        out.put("neighbors", neighbors.stream().limit(cappedLimit).collect(Collectors.toList()));

                        List<Map<String, Object>> neighborsWithSongs = getNeighborsWithSharedSongs(userId);
                        out.put("neighborsWithSongsCount", neighborsWithSongs.size());
                        out.put("neighborsWithSongs", neighborsWithSongs.stream().limit(cappedLimit).collect(Collectors.toList()));

                        List<Map<String, Object>> direct = recommendDirectNeighbors(userId, cappedLimit);
                        List<String> directIds = direct.stream().map(m -> (String) m.get("id")).collect(Collectors.toList());
                        out.put("directRecommendations", direct);
                        out.put("directCount", direct.size());

                        Map<String, Map<String, Double>> adj = buildFullAdjacencyGraph();
                        out.put("graphNodes", adj.size());
                        out.put("graphEdges", adj.values().stream().mapToInt(Map::size).sum());
                        out.put("allUserKeysSample", adj.keySet().stream().limit(cappedLimit).collect(Collectors.toList()));

                        String sourceKey = resolveKey(adj, resolved != null ? resolved : userId);
                        Map<String, Object> closest = null;
                        List<Map<String, Object>> shortest = List.of();
                        List<Map<String, Object>> edgeSample = List.of();
                        int userNeighborCount = 0;

                        if (sourceKey != null) {
                                Map<String, Object> dijkstraRes = Dijkstra.dijkstra(adj, sourceKey);
                                @SuppressWarnings("unchecked")
                                Map<String, Double> distances = (Map<String, Double>) dijkstraRes.get("distances");

                                List<Map.Entry<String, Double>> shortestEntries = distances.entrySet().stream()
                                        .filter(e -> !e.getKey().equals(sourceKey) && !Double.isInfinite(e.getValue()))
                                        .sorted(Map.Entry.comparingByValue())
                                        .limit(cappedLimit)
                                        .collect(Collectors.toList());

                                shortest = mapper.decorateRankedUsers(shortestEntries, "distance");
                                closest = shortest.isEmpty() ? null : shortest.get(0);

                                Map<String, Double> userEdges = adj.getOrDefault(sourceKey, Map.of());
                                userNeighborCount = userEdges.size();
                                List<Map.Entry<String, Double>> edgeEntries = userEdges.entrySet().stream()
                                        .sorted(Map.Entry.comparingByValue())
                                        .limit(cappedLimit)
                                        .collect(Collectors.toList());
                                edgeSample = mapper.decorateRankedUsers(edgeEntries, "weight");
                        }

                        out.put("shortestRecommendations", shortest);
                        out.put("shortestCount", shortest.size());
                        out.put("closest", closest);
                        out.put("userNeighborCount", userNeighborCount);
                        out.put("userEdgeSample", edgeSample);

                        Set<String> candidateUsers = new HashSet<>(adj.keySet());
                        if (sourceKey != null) {
                                candidateUsers.remove(sourceKey);
                        }
                        out.put("candidateUsersCount", candidateUsers.size());
                        out.put("candidateUsersSample", candidateUsers.stream().limit(cappedLimit).collect(Collectors.toList()));

                        Set<String> relevantNeighbors = new HashSet<>();
                        relevantNeighbors.addAll(directIds);
                        relevantNeighbors.addAll(shortest.stream().map(m -> (String) m.get("id")).collect(Collectors.toList()));
                        Map<String, String> labelById = mapper.resolveLabels(relevantNeighbors);
                        Map<String, List<String>> sharedSongs = new LinkedHashMap<>();
                        for (Map<String, Object> entry : neighborsWithSongs) {
                                Object nbObj = entry.get("neighbor");
                                if (!(nbObj instanceof String neighbor)) continue;
                                if (!relevantNeighbors.contains(neighbor)) continue;
                                @SuppressWarnings("unchecked")
                                List<String> songs = entry.get("songs") instanceof List ? (List<String>) entry.get("songs") : List.of();
                                String label = labelById.getOrDefault(neighbor, neighbor);
                                sharedSongs.put(label, songs);
                        }
                        out.put("sharedSongs", sharedSongs);

                        out.put("shortestRecommendationsRaw", shortest.stream().map(m -> (String) m.get("id")).collect(Collectors.toList()));
                        out.put("directRecommendationsRaw", directIds);

                        return out;
                }

                private Map<String, Object> fetchUserNodeDetails(String userId) {
                        if (userId == null) {
                                return Map.of();
                        }

                        String cypher = "MATCH (p:User) WHERE toLower(coalesce(p.id,p.nombre,p.name)) = toLower($userId) " +
                                "RETURN labels(p) AS labels, properties(p) AS props LIMIT 1";

                        List<Map<String, Object>> rows = db.readList(cypher, Map.of("userId", userId), record -> {
                                Map<String, Object> row = new HashMap<>();
                                row.put("labels", record.get("labels").asList(v -> v.asString()));
                                row.put("properties", record.get("props").asMap());
                                return row;
                        });

                        return rows.isEmpty() ? Map.of() : rows.get(0);
                }
}
