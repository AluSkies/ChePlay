package org.cheplay.AlgorithmTests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class BFSTest {
    @Test
    void runBFS() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.graph.BFS"));
    }
}
