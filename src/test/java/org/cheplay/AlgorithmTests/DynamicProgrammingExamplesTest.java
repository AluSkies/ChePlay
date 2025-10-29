package org.cheplay.AlgorithmTests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class DynamicProgrammingExamplesTest {
    @Test
    void runDynamicProgrammingExamples() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.dynamic.DynamicProgrammingExamples"));
    }
}
