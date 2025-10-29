package org.cheplay.AlgorithmTests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class BranchAndBoundExamplesTest {
    @Test
    void runBranchAndBoundExamples() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.branchandbound.BranchAndBoundExamples"));
    }
}
