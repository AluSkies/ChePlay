package org.cheplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class GreedyExamplesTest {
    @Test
    void runGreedyExamples() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.greedy.GreedyExamples"));
    }
}
