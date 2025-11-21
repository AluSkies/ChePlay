package org.cheplay.algorithm.greedy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Este algoritmo encuentra eficientemente los K elementos principales de
 * una colección usando un min-heap de tamaño K. Mantiene solo los K elementos
 * más grandes vistos hasta ahora, haciéndolo más eficiente que ordenar toda
 * la colección cuando K es mucho menor que el número total de elementos.
 * 
 * Complejidad Temporal: O(elementos × log(topK)) - Complejidad temporal
 *                       linealítmica (más eficiente que O(elementos × log(elementos))
 *                       cuando topK << elementos)
 * Complejidad Espacial: O(topK) - Complejidad espacial lineal
 *                       (solo almacena K elementos en el heap)
 */
public class GreedyExamples {

    /**
     * Encuentra las K canciones principales por conteo de reproducción usando
     * un enfoque codicioso basado en heap.
     * 
     */
    public static List<Map.Entry<String, Integer>> topKGreedy(Map<String, Integer> songs, int k) {
        if (songs == null || songs.isEmpty() || k <= 0) return List.of();

        // min-heap por cantidad de reproducciones
        PriorityQueue<Map.Entry<String, Integer>> heap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : songs.entrySet()) {
            if (heap.size() < k) {
                heap.offer(entry);
            } else if (entry.getValue() > heap.peek().getValue()) {
                heap.poll();  // saco el menor
                heap.offer(entry);
            }
        }

        // paso final: ordenar descendentemente el resultado
        List<Map.Entry<String, Integer>> result = new ArrayList<>(heap);
        result.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        return result;
    }
}
