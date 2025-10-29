package org.cheplay.AlgorithmTests;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlgorithmIntegrationTest {

    private final List<String> classesToRun = List.of(
        "org.cheplay.algorithm.backtracking.BacktrackingExamples",
        "org.cheplay.algorithm.branchandbound.BranchAndBoundExamples",
        "org.cheplay.algorithm.divideandconquer.MergeSort",
        "org.cheplay.algorithm.divideandconquer.QuickSort",
        "org.cheplay.algorithm.dynamic.DynamicProgrammingExamples",
        "org.cheplay.algorithm.graph.BFS",
        "org.cheplay.algorithm.graph.DFS",
        "org.cheplay.algorithm.greedy.GreedyExamples",
        "org.cheplay.algorithm.mst.Kruskal",
        "org.cheplay.algorithm.mst.Prim",
        "org.cheplay.algorithm.shortestpath.Dijkstra"
    );

    @Test
    public void runAllAlgorithmExamples_noExceptions() {
        for (String className : classesToRun) {
            Assertions.assertDoesNotThrow(() -> runClassIfPossible(className), className + " should run without throwing");
        }
    }

    private void runClassIfPossible(String className) throws Exception {
        Class<?> cls = Class.forName(className);

        // Try public static void main(String[])
        try {
            Method main = cls.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
            return;
        } catch (NoSuchMethodException ignored) {
        }

        // Try common no-arg methods
        String[] candidateNames = {"runExamples", "run", "execute", "demo", "examples", "start"};
        for (String name : candidateNames) {
            try {
                Method m = cls.getMethod(name);
                if ((m.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0) {
                    m.invoke(null);
                } else {
                    Object inst = cls.getDeclaredConstructor().newInstance();
                    m.invoke(inst);
                }
                return;
            } catch (NoSuchMethodException ignored) {
            }
        }

        // As a last resort, try to instantiate the class (to catch ctor errors)
        cls.getDeclaredConstructor().newInstance();
    }
}
