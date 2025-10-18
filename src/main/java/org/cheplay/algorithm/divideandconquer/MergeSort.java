package org.cheplay.algorithm.divideandconquer;

import java.util.List;
import java.util.ArrayList;

public class MergeSort {
    public static void mergeSort(List<Integer> arr, int left, int right) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }

    private static void merge(List<Integer> arr, int left, int mid, int right) {
        List<Integer> tmp = new ArrayList<>();
        int i = left, j = mid + 1;
        while (i <= mid && j <= right) {
            if (arr.get(i) <= arr.get(j)) tmp.add(arr.get(i++));
            else tmp.add(arr.get(j++));
        }
        while (i <= mid) tmp.add(arr.get(i++));
        while (j <= right) tmp.add(arr.get(j++));
        for (int k = left; k <= right; k++) arr.set(k, tmp.get(k - left));
    }
}
