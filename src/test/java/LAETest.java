// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import static org.junit.jupiter.api.Assertions.*;

// import spl.lae.LinearAlgebraEngine;
// import parser.ComputationNode;
// import java.util.List;

// public class LAETest {

//     private LinearAlgebraEngine engine;

//     @BeforeEach
//     void setUp() {
//         // Initialize with 4 threads for concurrent execution testing
//         engine = new LinearAlgebraEngine(4);
//     }

//     @Test
//     void testBasicAddition() {
//         double[][] d1 = {{1, 2}, {3, 4}};
//         double[][] d2 = {{10, 20}, {30, 40}};
        
//         ComputationNode node1 = new ComputationNode(d1);
//         ComputationNode node2 = new ComputationNode(d2);
//         ComputationNode addNode = new ComputationNode("+", List.of(node1, node2));

//         engine.run(addNode);
        
//         double[][] res = addNode.getMatrix();
//         assertEquals(11.0, res[0][0]);
//         assertEquals(44.0, res[1][1]);
//     }

//     @Test
//     void testMatrixMultiplication() {
//         // 2x3 matrix
//         double[][] d1 = {{1, 2, 3}, {4, 5, 6}};
//         // 3x2 matrix
//         double[][] d2 = {{7, 8}, {9, 10}, {11, 12}};
        
//         ComputationNode n1 = new ComputationNode(d1);
//         ComputationNode n2 = new ComputationNode(d2);
//         ComputationNode mult = new ComputationNode("*", List.of(n1, n2));

//         engine.run(mult);
        
//         double[][] res = mult.getMatrix();
//         // Row 0, Col 0: 1*7 + 2*9 + 3*11 = 58
//         assertEquals(58.0, res[0][0]);
//         // Row 1, Col 1: 4*8 + 5*10 + 6*12 = 154
//         assertEquals(154.0, res[1][1]);
//     }

//     @Test
//     void testTranspose() {
//         double[][] d = {{1, 2, 3}, {4, 5, 6}};
//         ComputationNode n = new ComputationNode(d);
//         ComputationNode t = new ComputationNode("T", List.of(n));

//         engine.run(t);

//         double[][] res = t.getMatrix();
//         // Dimensions should flip from 2x3 to 3x2
//         assertEquals(3, res.length);
//         assertEquals(2, res[0].length);
//         assertEquals(4.0, res[0][1]);
//     }

//     @Test
//     void testChainOperations() {
//         // Testing (A + B) * C
//         double[][] a = {{1, 0}, {0, 1}};
//         double[][] b = {{1, 1}, {1, 1}};
//         double[][] c = {{5, 0}, {0, 5}};

//         ComputationNode nA = new ComputationNode(a);
//         ComputationNode nB = new ComputationNode(b);
//         ComputationNode nC = new ComputationNode(c);

//         ComputationNode add = new ComputationNode("+", List.of(nA, nB));
//         ComputationNode root = new ComputationNode("*", List.of(add, nC));

//         engine.run(root);

//         double[][] res = root.getMatrix();
//         // (A+B) is {{2,1},{1,2}}, then *5 is {{10,5},{5,10}}
//         assertEquals(10.0, res[0][0]);
//         assertEquals(5.0, res[0][1]);
//         assertEquals(10.0, res[1][1]);
//     }

//     @Test
//     void testReporting() {
//         double[][] d = {{1, 1}, {1, 1}};
//         ComputationNode n = new ComputationNode(d);
//         engine.run(n);

//         String report = engine.getWorkerReport();
//         assertNotNull(report);
//         assertTrue(report.contains("Fairness Score"));
//     }
// }