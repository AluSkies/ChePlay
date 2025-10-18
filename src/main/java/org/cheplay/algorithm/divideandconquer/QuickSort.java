package org.cheplay.algorithm.divideandconquer;

import java.util.List;
import java.util.Collections;

public class QuickSort {
    public static void quicksort(List<Integer> arr, int low, int high) {
        if (low >= high) return;
        int p = partition(arr, low, high);
        quicksort(arr, low, p - 1);
        quicksort(arr, p + 1, high);
    }

    private static int partition(List<Integer> arr, int low, int high) {
        int pivot = arr.get(high);
        int i = low;
        for (int j = low; j < high; j++) {
            if (arr.get(j) <= pivot) {
                Collections.swap(arr, i, j);
                i++;
            }
        }
        Collections.swap(arr, i, high);
        return i;
    }
}
