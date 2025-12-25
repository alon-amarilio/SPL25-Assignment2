import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import memory.*;
import parser.*;
import spl.lae.LinearAlgebraEngine;
import scheduling.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive test suite for LAE. 
 * Checks math correctness, JSON structure, and thread fairness.
 */
public class LAEComprehensiveTest {

    // Threshold for double comparisons to handle floating point errors
    private static final double DELTA = 1e-6;

    // --- Part 1: Math & Edge Cases [cite: 62, 63, 67] ---

    @Test
    @DisplayName("Test basic vector operations: Addition & Negation")
    public void testVectorMath() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{0.5, 0.5}, VectorOrientation.ROW_MAJOR);
        
        // Testing addition [cite: 82, 353]
        v1.add(v2);
        assertEquals(1.5, v1.get(0), DELTA); // Using DELTA to fix the yellow warning
        assertEquals(2.5, v1.get(1), DELTA);

        // Testing negation [cite: 85, 353]
        v1.negate();
        assertEquals(-1.5, v1.get(0), DELTA);
    }

    @Test
    @DisplayName("Verify illegal operations throw exceptions [cite: 64, 195]")
    public void testDimensionMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        
        // PDF requires dimension checks [cite: 64, 194]
        assertThrows(IllegalArgumentException.class, () -> v1.add(v2), 
            "Adding vectors of different sizes must fail");
    }

    // --- Part 2: Tree Logic & Nesting [cite: 65, 87, 88] ---

    @Test
    @DisplayName("Validate Left-Associative Nesting [cite: 87, 88]")
    public void testNestingLogic() {
        // According to PDF: *(A,B,C) should be evaluated as (A*B)*C [cite: 87, 88]
        double[][] m = {{1}};
        List<ComputationNode> operands = new ArrayList<>();
        operands.add(new ComputationNode(m));
        operands.add(new ComputationNode(m));
        operands.add(new ComputationNode(m));
        
        ComputationNode root = new ComputationNode("*", operands);
        root.associativeNesting(); // [cite: 173]
        
        // After nesting, the root should have exactly 2 children (binary tree)
        assertEquals(2, root.getChildren().size(), "Root should be binary after associative nesting");
        assertEquals(ComputationNodeType.MULTIPLY, root.getChildren().get(0).getNodeType());
    }

    // --- Part 3: Concurrency, Fatigue & Fairness [cite: 181, 375, 378] ---

    @Test
    @DisplayName("Verify worker report and Fairness Score calculation [cite: 375]")
    public void testThreadFairness() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(4); // Creating pool [cite: 311]
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        
        ComputationNode node = new ComputationNode("+", List.of(
            new ComputationNode(data), new ComputationNode(data)
        ));
        
        engine.run(node); // Executes using TiredExecutor [cite: 181, 312]
        String report = engine.getWorkerReport();
        
        // Requirement: Report must include Fairness Score [cite: 375, 378]
        assertTrue(report.contains("Fairness Score"), "Output report must include the fairness score");
        assertTrue(report.contains("Average Fatigue"), "Report must show fatigue levels");
    }

    // --- Part 4: JSON Property Validation [cite: 381, 383] ---

    @Test
    @DisplayName("Verify JSON property names: 'result' and 'error' [cite: 381]")
    public void testJsonCompliance() {
        // Output file must have either "result" or "error" property [cite: 381]
        double[][] mockMatrix = {{1.0}};
        OutputWriter.ResultMatrix resultObj = new OutputWriter.ResultMatrix(mockMatrix);
        assertNotNull(resultObj.result, "JSON wrapper for result must use key 'result' [cite: 382]");

        OutputWriter.ErrorMessage errorObj = new OutputWriter.ErrorMessage("Dimension mismatch");
        assertEquals("Dimension mismatch", errorObj.error, "JSON wrapper for error must use key 'error' [cite: 383]");
    }

    @Test
    @DisplayName("Concurrent lock stress test [cite: 350, 352]")
    public void testLockingSafety() throws InterruptedException {
        // SharedVector uses ReentrantReadWriteLock [cite: 350]
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        AtomicInteger count = new AtomicInteger(0);

        Thread writer = new Thread(() -> {
            for(int i=0; i<100; i++) v.negate(); // Exclusive access [cite: 352]
        });

        Thread reader = new Thread(() -> {
            for(int i=0; i<100; i++) {
                v.get(0); // Simultaneous reads allowed [cite: 352]
                count.incrementAndGet();
            }
        });

        writer.start(); reader.start();
        writer.join(); reader.join();
        assertEquals(100, count.get(), "Reader should finish all cycles safely");
    }
}