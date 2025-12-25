import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import memory.*;

public class DimensionErrorTest {

    @Test
    public void testAddDifferentLengths() {
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.add(v2), "Should fail for 2 vs 3 elements");
    }

    @Test
    public void testIncompatibleMatrixMul() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        // Matrix is 3x3, vector is size 2 -> incompatible
        SharedMatrix m = new SharedMatrix(new double[][]{{1,0,0},{0,1,0},{0,0,1}});
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m));
    }

    @Test
    public void testEmptyMatrixOperations() {
        SharedMatrix empty = new SharedMatrix(new double[0][0]);
        assertEquals(0, empty.length(), "Empty matrix should have 0 length");
    }
}