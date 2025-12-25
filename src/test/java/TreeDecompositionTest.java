import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import parser.*;
import java.util.ArrayList;
import java.util.List;

public class TreeDecompositionTest {

    @Test
    public void testThreeNodeNesting() {
        // Testing (A+B)+C structure
        double[][] m = {{1}};
        List<ComputationNode> ops = new ArrayList<>(List.of(
            new ComputationNode(m), new ComputationNode(m), new ComputationNode(m)
        ));
        ComputationNode root = new ComputationNode("+", ops);
        root.associativeNesting();
        assertEquals(2, root.getChildren().size(), "Root must become binary");
    }

    @Test
    public void testDeepNestingStructure() {
        // Testing 5 nodes to see if it remains binary all the way
        double[][] m = {{1}};
        List<ComputationNode> ops = new ArrayList<>();
        for(int i=0; i<5; i++) ops.add(new ComputationNode(m));
        ComputationNode root = new ComputationNode("*", ops);
        root.associativeNesting();
        assertEquals(2, root.getChildren().size());
        assertEquals(ComputationNodeType.MULTIPLY, root.getChildren().get(0).getNodeType());
    }

    @Test
    public void testFindResolvableOnReadyTree() {
        double[][] m = {{1}};
        ComputationNode ready = new ComputationNode("+", List.of(new ComputationNode(m), new ComputationNode(m)));
        ComputationNode root = new ComputationNode("*", List.of(ready, new ComputationNode(m)));
        // Should pick the ADD node because it has concrete matrix children
        assertEquals(ComputationNodeType.ADD, root.findResolvable().getNodeType());
    }
}