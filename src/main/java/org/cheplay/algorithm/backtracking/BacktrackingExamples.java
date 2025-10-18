package org.cheplay.algorithm.backtracking;

import java.util.*;

public class BacktrackingExamples {
    public static List<List<String>> solveNQueens(Map<String, Object> params) {
        int n = (int) params.getOrDefault("n", 4);
        List<List<String>> solutions = new ArrayList<>();
        int[] cols = new int[n];
        place(0, n, cols, solutions);
        return solutions;
    }

    private static void place(int row, int n, int[] cols, List<List<String>> solutions) {
        if (row == n) {
            List<String> board = new ArrayList<>();
            for (int r = 0; r < n; r++) {
                char[] line = new char[n];
                Arrays.fill(line, '.');
                line[cols[r]] = 'Q';
                board.add(new String(line));
            }
            solutions.add(board);
            return;
        }
        for (int c = 0; c < n; c++) {
            cols[row] = c;
            if (valid(row, cols)) place(row + 1, n, cols, solutions);
        }
    }

    private static boolean valid(int row, int[] cols) {
        for (int i = 0; i < row; i++) {
            if (cols[i] == cols[row]) return false;
            if (Math.abs(cols[i] - cols[row]) == row - i) return false;
        }
        return true;
    }
}
