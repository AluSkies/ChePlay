package org.cheplay.algorithm.divideandconquer;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class QuickSort {

    public static LinkedHashMap<String, Integer> quicksort(LinkedHashMap<String, Integer> map) {
        if (map.size() <= 1) return map;

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());

        int pivotIndex = entries.size() / 2;
        int pivotValue = entries.get(pivotIndex).getValue();

        LinkedHashMap<String, Integer> menores = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> iguales = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> mayores = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : entries) {
            if (entry.getValue() < pivotValue)
                menores.put(entry.getKey(), entry.getValue());
            else if (entry.getValue().equals(pivotValue))
                iguales.put(entry.getKey(), entry.getValue());
            else
                mayores.put(entry.getKey(), entry.getValue());
        }

        menores = quicksort(menores);
        mayores = quicksort(mayores);

        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();
        resultado.putAll(menores);
        resultado.putAll(iguales);
        resultado.putAll(mayores);

        return resultado;
    }
}
