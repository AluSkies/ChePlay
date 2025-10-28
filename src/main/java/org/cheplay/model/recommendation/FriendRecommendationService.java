package org.cheplay.model.recommendation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public FriendRecommendationService(DbConnector db) {
        this.db = Objects.requireNonNull(db, "DbConnector");
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
    public List<String> recommendDirectNeighbors(String userId, int k) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        Map<String, Object> params = Map.of("userId", userId);
        List<Map.Entry<String, Double>> list = db.readList(USER_NEIGHBORS_CYPHER, params, (Record r) ->
                Map.entry(r.get("neighbor").asString(), r.get("weight").asDouble())
        );

        // readList returns List<Map.Entry<..>> — we sort by value then collect top-k keys
        return list.stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .limit(k <= 0 ? Integer.MAX_VALUE : k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Recomendación por distancia acumulada (Dijkstra) usando el grafo completo. Devuelve top-k usuarios
     * con menor distancia (excluye al propio userId).
     */
    public List<String> recommendByShortestPath(String userId, int k) {
                if (userId == null) throw new IllegalArgumentException("userId is required");
                Map<String, Map<String, Double>> adj = buildFullAdjacencyGraph();

                // Resolve actual key in adjacency map (case-insensitive fallback)
                String sourceKey = resolveKey(adj, userId);
                if (sourceKey == null) return List.of();

                Map<String, Object> res = Dijkstra.dijkstra(adj, sourceKey);
                @SuppressWarnings("unchecked")
                Map<String, Double> distances = (Map<String, Double>) res.get("distances");

                return distances.entrySet().stream()
                                .filter(e -> !e.getKey().equals(sourceKey) && !Double.isInfinite(e.getValue()))
                                .sorted(Map.Entry.comparingByValue())
                                .limit(k <= 0 ? Integer.MAX_VALUE : k)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
    }

        /**
         * Encuentra la persona más cercana al userId usando Dijkstra y devolviendo el único mejor candidato.
         * Retorna null si no hay ningún vecino alcanzable.
         */
        public String findClosestUser(String userId) {
                if (userId == null) throw new IllegalArgumentException("userId is required");
                Map<String, Map<String, Double>> adj = buildFullAdjacencyGraph();
                String sourceKey = resolveKey(adj, userId);
                if (sourceKey == null) return null;

                Map<String, Object> res = Dijkstra.dijkstra(adj, sourceKey);
                @SuppressWarnings("unchecked")
                Map<String, Double> distances = (Map<String, Double>) res.get("distances");

                return distances.entrySet().stream()
                                .filter(e -> !e.getKey().equals(sourceKey) && !Double.isInfinite(e.getValue()))
                                .min(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse(null);
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
}
