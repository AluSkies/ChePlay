package org.cheplay.algorithm.graph;

import java.util.*;

/**
 * Este algoritmo recorre un grafo explorando lo más profundo posible a lo
 * largo de cada rama antes de retroceder. Utiliza recursión (o una pila
 * implícita) para mantener el camino de exploración, permitiendo una
 * exploración profunda del grafo.
 * 
 * Complejidad Temporal: O(nodos + aristas) - Complejidad temporal lineal
 * Complejidad Espacial: O(nodos) - Complejidad espacial lineal (para la
 * pila de recursión y el conjunto de visitados)
 * 
 */
public class DFS {
    public static void dfs(String node,
                           Map<String, Map<String, Double>> adj,
                           Set<String> visited,
                           List<String> order) {
        if (node == null || visited.contains(node)) return;
        visited.add(node);
        order.add(node);
        for (String nb : adj.getOrDefault(node, Collections.emptyMap()).keySet()) {
            if (!visited.contains(nb)) dfs(nb, adj, visited, order);
        }
    }
}
