package org.cheplay.AlgorithmTests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class BacktrackingExamplesTest {
    @Test
    void runBacktrackingExamples() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.backtracking.BacktrackingExamples"));
    }
}
