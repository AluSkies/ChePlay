package org.cheplay.AlgorithmTests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class QuickSortTest {
    @Test
    void runQuickSort() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.divideandconquer.QuickSort"));
    }
}
