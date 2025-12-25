import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import memory.*;

public class VectorAndMatrixTest {
    private static final double DELTA = 1e-6;

    // --- Addition Tests ---
    @Test
    public void testBasicAddition() {
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3, 4}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);
        assertEquals(4.0, v1.get(0), DELTA);
        assertEquals(6.0, v1.get(1), DELTA);
    }

    @Test
    public void testAdditionWithZeros() {
        SharedVector v1 = new SharedVector(new double[]{0, 0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{5, -5}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);
        assertEquals(5.0, v1.get(0), DELTA);
        assertEquals(-5.0, v1.get(1), DELTA);
    }

    @Test
    public void testAdditionNegativeResults() {
        SharedVector v1 = new SharedVector(new double[]{-10, -20}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{5, 5}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);
        assertEquals(-5.0, v1.get(0), DELTA);
    }

    // --- Negation & Transpose ---
    @Test
    public void testNegatePositiveAndNegative() {
        SharedVector v = new SharedVector(new double[]{10, -10, 0}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertEquals(-10.0, v.get(0), DELTA);
        assertEquals(10.0, v.get(1), DELTA);
        assertEquals(0.0, v.get(2), DELTA);
    }

    @Test
    public void testDoubleTranspose() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
        v.transpose(); // should go back to row
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    // --- Multiplication ---
    @Test
    public void testMultiplyByIdentity() {
        SharedVector v = new SharedVector(new double[]{5, 10}, VectorOrientation.ROW_MAJOR);
        SharedMatrix id = new SharedMatrix(new double[][]{{1, 0}, {0, 1}});
        v.vecMatMul(id);
        assertEquals(5.0, v.get(0), DELTA);
        assertEquals(10.0, v.get(1), DELTA);
    }
}