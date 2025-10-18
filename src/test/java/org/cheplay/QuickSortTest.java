package org.cheplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class QuickSortTest {
    @Test
    void runQuickSort() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.divideandconquer.QuickSort"));
    }
}
