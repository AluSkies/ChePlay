package org.cheplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BranchAndBoundExamplesTest {
    @Test
    void runBranchAndBoundExamples() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.branchandbound.BranchAndBoundExamples"));
    }
}
