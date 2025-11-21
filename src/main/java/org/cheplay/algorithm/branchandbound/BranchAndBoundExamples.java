package org.cheplay.algorithm.branchandbound;

import java.util.*;

/**
 * Este algoritmo encuentra la mejor solución de suma de subconjuntos explorando
 * el espacio de soluciones usando una cola de prioridad, priorizando estados
 * con sumas más altas. Utiliza técnicas de acotación para podar ramas que no
 * pueden llevar a mejores soluciones, haciéndolo más eficiente que el
 * backtracking puro en la práctica.
 * 
 * Complejidad Temporal: O(2^elementos) peor caso - Complejidad temporal
 *                       exponencial (típicamente mucho mejor en la práctica
 *                       debido al podado)
 * Complejidad Espacial: O(elementos) - Complejidad espacial lineal
 *                       (para la cola de prioridad, limitada por la
 *                       implementación)
 * 
 */
public class BranchAndBoundExamples {
    public static Map<String, Object> simpleSubsetSumBB(Map<String, Object> params) {
        int target = (int) params.getOrDefault("target", 0);
        List<Integer> nums = (List<Integer>) params.getOrDefault("nums", Collections.emptyList());
        int n = nums.size();
        class State {
            int idx; int sum; List<Integer> chosen;
            State(int i, int s, List<Integer> c){ idx=i; sum=s; chosen=c; }
        }
        Comparator<State> cmp = Comparator.comparingInt(s->-s.sum);
        PriorityQueue<State> pq = new PriorityQueue<>(cmp);
        pq.add(new State(0,0,new ArrayList<>()));
        int best = 0; List<Integer> bestChosen = new ArrayList<>();
        while (!pq.isEmpty()) {
            State s = pq.poll();
            if (s.sum > best && s.sum <= target) { best = s.sum; bestChosen = s.chosen; }
            if (s.idx >= n || s.sum >= target) continue;
            int with = s.sum + nums.get(s.idx);
            if (with <= target) {
                List<Integer> c1 = new ArrayList<>(s.chosen);
                c1.add(s.idx);
                pq.add(new State(s.idx + 1, with, c1));
            }
            pq.add(new State(s.idx + 1, s.sum, new ArrayList<>(s.chosen)));
        }
        return Map.of("bestSum", best, "indices", bestChosen);
    }
}
