package org.cheplay.algorithm.divideandconquer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuickSort {

    public static LinkedHashMap<String, Integer> quicksort(LinkedHashMap<String, Integer> map) {
        if (map == null || map.size() <= 1) return map == null ? new LinkedHashMap<>() : new LinkedHashMap<>(map);

        // paso a lista porque es más mejor, más cool
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        List<Map.Entry<String, Integer>> sorted = quicksortEntries(entries);

        //remap a LinkedHashMap para mantener el orden
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : sorted) out.put(e.getKey(), e.getValue());
        return out;
    }

    private static List<Map.Entry<String, Integer>> quicksortEntries(List<Map.Entry<String, Integer>> entries) {
        if (entries.size() <= 1) return entries;

        int pivotIndex = entries.size() / 2;
        Integer pivotValue = safe(entries.get(pivotIndex).getValue());

        List<Map.Entry<String, Integer>> mayores = new ArrayList<>();
        List<Map.Entry<String, Integer>> iguales  = new ArrayList<>();
        List<Map.Entry<String, Integer>> menores = new ArrayList<>();

        for (Map.Entry<String, Integer> e : entries) {
            int v = safe(e.getValue());
            if (v > pivotValue)      mayores.add(e);   // DESC: primero los mayores
            else if (v == pivotValue) iguales.add(e);
            else                      menores.add(e);
        }

        // recursión
        List<Map.Entry<String, Integer>> left  = quicksortEntries(mayores);
        List<Map.Entry<String, Integer>> mid   = iguales;            // ya está
        List<Map.Entry<String, Integer>> right = quicksortEntries(menores);

        // recombinar en orden DESC
        List<Map.Entry<String, Integer>> res = new ArrayList<>(entries.size());
        res.addAll(left);
        res.addAll(mid);
        res.addAll(right);
        return res;
    }

    private static int safe(Integer v) { return v == null ? 0 : v; }
}
