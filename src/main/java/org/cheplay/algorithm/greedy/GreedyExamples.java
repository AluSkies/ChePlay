package org.cheplay.algorithm.greedy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class GreedyExamples {

    /**
     * @param songs  Map<songId, plays>
     * @param k      número de canciones a devolver
     * @return Lista ordenada descendentemente por plays (id, plays)
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
