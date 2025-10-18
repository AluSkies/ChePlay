package org.cheplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BacktrackingExamplesTest {
    @Test
    void runBacktrackingExamples() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.backtracking.BacktrackingExamples"));
    }
}
