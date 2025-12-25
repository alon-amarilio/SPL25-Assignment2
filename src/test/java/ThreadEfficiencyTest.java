import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import spl.lae.LinearAlgebraEngine;
import parser.ComputationNode;
import java.util.List;

public class ThreadEfficiencyTest {

    @Test
    public void testFairnessScoreCalculation() {
        // The assignment focuses on effective parallelism and fairness
        LinearAlgebraEngine engine = new LinearAlgebraEngine(4);
        double[][] data = {{1.0, 1.0}, {1.0, 1.0}};
        
        // Running a simple task to trigger fatigue tracking
        engine.run(new ComputationNode(data));
        String report = engine.getWorkerReport();
        
        assertTrue(report.contains("Fairness Score"), "Report missing required Fairness Score metric");
    }

    @Test
    public void testMultipleWorkersReport() {
        int threads = 3;
        LinearAlgebraEngine engine = new LinearAlgebraEngine(threads);
        String report = engine.getWorkerReport();
        
        // Verify all threads are initialized in the report
        for(int i=0; i < threads; i++) {
            assertTrue(report.contains("Worker " + i), "Worker " + i + " data missing from report");
        }
    }

    @Test
    public void testFatigueAccumulation() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        // Using a chain of additions to ensure threads actually work
        double[][] m = {{1}};
        ComputationNode task = new ComputationNode("+", List.of(new ComputationNode(m), new ComputationNode(m)));
        
        engine.run(task);
        String report = engine.getWorkerReport();
        
        // Fatigue shouldn't be zero if work was actually done
        assertFalse(report.contains("Average Fatigue: 0.00"), "Fatigue should accumulate after tasks");
    }
}