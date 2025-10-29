package org.cheplay.AlgorithmTests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class DijkstraTest {
    @Test
    void runDijkstra() {
        assertDoesNotThrow(() -> ReflectiveTestBase.runClass("org.cheplay.algorithm.shortestpath.Dijkstra"));
    }
}
