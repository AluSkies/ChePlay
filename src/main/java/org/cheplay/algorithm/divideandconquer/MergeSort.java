package org.cheplay.algorithm.divideandconquer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Este algoritmo ordena elementos dividiendo recursivamente la lista en
 * mitades, ordenando cada mitad, y luego fusionando las mitades ordenadas.
 * Garantiza ordenamiento estable y rendimiento consistente O(n log n).
 * Ordena en orden descendente por valor.
 * 
 * Complejidad Temporal: O(elementos × log(elementos)) - Complejidad temporal
 *                       linealítmica
 * Complejidad Espacial: O(elementos) - Complejidad espacial lineal
 *                       (para arreglos temporales durante las operaciones de
 *                       fusión)
 */
public class MergeSort {

    public static LinkedHashMap<String, Integer> mergeSortByValue(Map<String, Integer> map) {
        
        List<String> keys = new ArrayList<>(map.keySet());
        
        mergeSort(keys, map);

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (String key : keys) {
            sortedMap.put(key, map.get(key));
        }
        
        return sortedMap;
    }

    private static void mergeSort(List<String> keys, Map<String, Integer> map) {
        if (keys.size() <= 1) return;

        int mid = keys.size() / 2;
        List<String> left = new ArrayList<>(keys.subList(0, mid));
        List<String> right = new ArrayList<>(keys.subList(mid, keys.size()));

        mergeSort(left, map);
        mergeSort(right, map);

        merge(keys, left, right, map);
    }

    private static void merge(List<String> result, List<String> left, List<String> right, Map<String, Integer> map) {
        int i = 0, j = 0, k = 0;

        while (i < left.size() && j < right.size()) {
        //cambio esto a órden descendente
        Integer li = map.get(left.get(i));
        Integer rj = map.get(right.get(j));
        if (li == null) li = 0;
        if (rj == null) rj = 0;

        
        if (li >= rj) {
            result.set(k++, left.get(i++));
        } else {
            result.set(k++, right.get(j++));
        }
        }

        
        while (i < left.size()) {
            result.set(k++, left.get(i++));
        }
        while (j < right.size()) {
            result.set(k++, right.get(j++));
        }
    }
}
