package org.cheplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class MergeSortTest {
    @Test
    void runMergeSort() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.divideandconquer.MergeSort"));
    }
}
