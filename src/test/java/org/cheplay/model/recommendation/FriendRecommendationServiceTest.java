package org.cheplay.model.recommendation;

import java.util.List;
import java.util.Map;

import org.cheplay.model.graph.GraphRelationship;
import org.cheplay.neo4j.DbConnector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FriendRecommendationServiceTest {

    @Test
    void recommendForAlan_directAndByShortestPath() {
        // Mock DbConnector
        DbConnector db = mock(DbConnector.class);

        // Global graph edges (GraphRelationship mapper is used in the service)
        List<GraphRelationship> globalEdges = List.of(
                new GraphRelationship("Alan", "Bob", 1.0 / (3 + 1.0)),   // overlap 3 -> 0.25
                new GraphRelationship("Alan", "Carol", 1.0 / (1 + 1.0)), // overlap 1 -> 0.5
                new GraphRelationship("Bob", "Carol", 1.0 / (2 + 1.0))   // overlap 2 -> 0.333...
        );

        // Neighbors for Alan (neighbor mapper used in service returns Map.Entry-like items, but readList will return these objects)
        List<Map.Entry<String, Double>> alanNeighbors = List.of(
                Map.entry("Bob", 1.0 / (3 + 1.0)),
                Map.entry("Carol", 1.0 / (1 + 1.0))
        );

        // Stub DbConnector.readList: when params is null -> return globalEdges; otherwise return neighbors
        when(db.readList(anyString(), isNull(), any())).thenReturn((List) globalEdges);
        when(db.readList(anyString(), anyMap(), any())).thenAnswer(inv -> {
            Map params = inv.getArgument(1);
            if (params != null && "Alan".equals(params.get("userId"))) {
                return (List) alanNeighbors;
            }
            return (List) globalEdges;
        });

        FriendRecommendationService svc = new FriendRecommendationService(db);

        // Direct neighbors for Alan
        List<String> direct = svc.recommendDirectNeighbors("Alan", 10);
        assertEquals(List.of("Bob", "Carol"), direct);

        // Shortest-path based recommendations for Alan
        List<String> viaPaths = svc.recommendByShortestPath("Alan", 10);
        assertEquals(List.of("Bob", "Carol"), viaPaths);
    }
}
