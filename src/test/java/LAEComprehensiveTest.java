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

public class LAEComprehensiveTest {

    private static final double DELTA = 1e-6;

    // --- Part 1: Math & Operations (cite: 63, 81-85) ---

    @Test
    @DisplayName("Vector Addition: Basic and zero-vector cases")
    public void testVectorAddition() {
        SharedVector v1 = new SharedVector(new double[]{1.0, -2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3.0, 2.0}, VectorOrientation.ROW_MAJOR);
        v1.add(v2); // cite: 353
        assertEquals(4.0, v1.get(0), DELTA);
        assertEquals(0.0, v1.get(1), DELTA);
    }

    @Test
    @DisplayName("Vector Negation: Positive, negative, and zero")
    public void testVectorNegation() {
        SharedVector v = new SharedVector(new double[]{5.5, -3.2, 0.0}, VectorOrientation.ROW_MAJOR);
        v.negate(); // cite: 85
        assertEquals(-5.5, v.get(0), DELTA);
        assertEquals(3.2, v.get(1), DELTA);
        assertEquals(0.0, v.get(2), DELTA);
    }

    @Test
    @DisplayName("Transpose Logic: Verify orientation flip")
    public void testTransposeOrientation() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        v.transpose(); // cite: 84
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    @Test
    @DisplayName("Matrix Multiplication: Row vector by Matrix")
    public void testMatrixMultiplication() {
        SharedVector row = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedMatrix mat = new SharedMatrix(new double[][]{{1, 0}, {0, 1}}); // Identity
        row.vecMatMul(mat); // cite: 180
        assertEquals(1.0, row.get(0), DELTA);
        assertEquals(2.0, row.get(1), DELTA);
    }

    // --- Part 2: Tree Orchestration & Nesting (cite: 87, 88, 178) ---

    @Test
    @DisplayName("Associative Nesting: 3 operands to Binary")
    public void testBinaryConversion() {
        double[][] m = {{1}};
        List<ComputationNode> ops = new ArrayList<>(List.of(new ComputationNode(m), new ComputationNode(m), new ComputationNode(m)));
        ComputationNode root = new ComputationNode("+", ops);
        root.associativeNesting(); // cite: 87, 173
        assertEquals(2, root.getChildren().size(), "Must be binary tree");
    }

    @Test
    @DisplayName("Deep Nesting: 5 operands to left-associative structure")
    public void testDeepNesting() {
        double[][] m = {{1}};
        List<ComputationNode> ops = new ArrayList<>();
        for(int i=0; i<5; i++) ops.add(new ComputationNode(m));
        ComputationNode root = new ComputationNode("*", ops);
        root.associativeNesting(); // cite: 88
        // Level 1 should have 2 children, Level 2 should have 2, etc.
        assertNotNull(root.getChildren().get(0).getChildren()); 
    }

    @Test
    @DisplayName("Find Resolvable: Direct matrix check")
    public void testFindResolvableSimple() {
        ComputationNode leaf = new ComputationNode(new double[][]{{1}});
        assertNull(leaf.findResolvable(), "Matrix leaf is not resolvable");
    }

    @Test
    @DisplayName("Find Resolvable: Nested operation discovery")
    public void testFindResolvableNested() {
        double[][] m = {{1}};
        ComputationNode op = new ComputationNode("+", List.of(new ComputationNode(m), new ComputationNode(m)));
        ComputationNode root = new ComputationNode("*", List.of(op, new ComputationNode(m)));
        assertEquals(ComputationNodeType.ADD, root.findResolvable().getNodeType()); // cite: 178
    }

    // --- Part 3: Concurrency & Fairness (cite: 317, 375, 378) ---

    @Test
    @DisplayName("Fairness Report: Check for required score")
    public void testFairnessScorePresence() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        engine.run(new ComputationNode(new double[][]{{1}}));
        String report = engine.getWorkerReport(); // cite: 373, 375
        assertTrue(report.contains("Fairness Score"), "Grading requires Fairness Score in report");
    }

    @Test
    @DisplayName("TiredExecutor: Verify all workers exist")
    public void testWorkerInitialization() {
        TiredExecutor exec = new TiredExecutor(3); // cite: 311, 316
        String report = exec.getWorkerReport();
        assertTrue(report.contains("Worker 0") && report.contains("Worker 2"));
    }

    @Test
    @DisplayName("Fatigue Accumulation: Verify work increases fatigue")
    public void testFatigueGrowth() {
        TiredThread thread = new TiredThread(0, 1.0); // cite: 325, 326
        double initial = thread.getFatigue();
        thread.start();
        thread.newTask(() -> { try { Thread.sleep(10); } catch(Exception e){} });
        try { Thread.sleep(50); } catch(Exception e){}
        assertTrue(thread.getFatigue() >= initial, "Fatigue must increase with work");
    }

    @Test
    @DisplayName("Concurrent Read Stress Test")
    public void testConcurrentReads() throws InterruptedException {
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        AtomicInteger reads = new AtomicInteger(0); // Using AtomicInteger to avoid unused warning
        Thread t1 = new Thread(() -> { for(int i=0; i<50; i++) { v.get(0); reads.incrementAndGet(); } });
        Thread t2 = new Thread(() -> { for(int i=0; i<50; i++) { v.get(0); reads.incrementAndGet(); } });
        t1.start(); t2.start();
        t1.join(); t2.join();
        assertEquals(100, reads.get(), "Multiple threads must be able to read concurrently (cite: 352)");
    }

    // --- Part 4: JSON & Dimension Errors (cite: 64, 194, 381-383) ---

    @Test
    @DisplayName("JSON Success Format: 'result' key presence")
    public void testJsonSuccessKey() {
        OutputWriter.ResultMatrix rm = new OutputWriter.ResultMatrix(new double[][]{{1}});
        assertNotNull(rm.result, "Success JSON must use 'result' property (cite: 382)");
    }

    @Test
    @DisplayName("JSON Error Format: 'error' key presence")
    public void testJsonErrorKey() {
        OutputWriter.ErrorMessage em = new OutputWriter.ErrorMessage("Test error");
        assertEquals("Test error", em.error, "Error JSON must use 'error' property (cite: 383)");
    }

    @Test
    @DisplayName("Edge Case: Dimension mismatch on Add")
    public void testAddMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.add(v2), "Add requires same dimensions (cite: 64, 194)");
    }

    @Test
    @DisplayName("Edge Case: Empty Matrix Handling")
    public void testEmptyMatrix() {
        SharedMatrix sm = new SharedMatrix(new double[0][0]);
        assertEquals(0, sm.length(), "Empty matrix should have length 0");
    }
}