package org.cheplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DynamicProgrammingExamplesTest {
    @Test
    void runDynamicProgrammingExamples() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.dynamic.DynamicProgrammingExamples"));
    }
}
