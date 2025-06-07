package pl.edu.uj.tcs.aiplayground.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TensorTest {

    @Test
    void testScalarConstructor() {
        Tensor t = new Tensor(5.0);
        assertEquals(1, t.rows);
        assertEquals(1, t.cols);
        assertEquals(5.0, t.data[0][0]);
        assertEquals(0.0, t.gradient[0][0]);
    }

    @Test
    void testMatrixConstructorValid() {
        double[][] data = {
                {1, 2},
                {3, 4}
        };
        Tensor t = new Tensor(data, 2, 2);
        assertEquals(2, t.rows);
        assertEquals(2, t.cols);
        assertArrayEquals(data, t.data);

        for (int i = 0; i < t.rows; i++) {
            for (int j = 0; j < t.cols; j++) {
                assertEquals(0.0, t.gradient[i][j]);
            }
        }
    }

    @Test
    void testMatrixConstructorInvalidShape() {
        double[][] data = {
                {1, 2},
                {3, 4}
        };
        // rows*cols=6 but data has only 4 elements
        assertThrows(IllegalArgumentException.class, () -> new Tensor(data, 2, 3));
    }

    @Test
    void testZeros() {
        Tensor zeros = Tensor.zeros(2, 3);
        assertEquals(2, zeros.rows);
        assertEquals(3, zeros.cols);
        for (int i = 0; i < zeros.rows; i++) {
            for (int j = 0; j < zeros.cols; j++) {
                assertEquals(0.0, zeros.data[i][j]);
            }
        }
    }

    @Test
    void testZerosLike() {
        double[][] data = {
                {1, 2},
                {3, 4}
        };
        Tensor original = new Tensor(data, 2, 2);
        Tensor zerosLike = Tensor.zerosLike(original);
        assertEquals(original.rows, zerosLike.rows);
        assertEquals(original.cols, zerosLike.cols);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(0.0, zerosLike.data[i][j]);
            }
        }
    }

    @Test
    void testAdd() {
        double[][] dataA = {{1, 2}, {3, 4}};
        double[][] dataB = {{5, 6}, {7, 8}};
        Tensor a = new Tensor(dataA, 2, 2);
        Tensor b = new Tensor(dataB, 2, 2);

        Tensor result = Tensor.add(a, b, null);

        assertEquals(2, result.rows);
        assertEquals(2, result.cols);
        assertEquals(6, result.data[0][0]);
        assertEquals(8, result.data[0][1]);
        assertEquals(10, result.data[1][0]);
        assertEquals(12, result.data[1][1]);
    }

    @Test
    void testAddShapeMismatch() {
        Tensor a = Tensor.zeros(2, 3);
        Tensor b = Tensor.zeros(3, 2);
        assertThrows(IllegalArgumentException.class, () -> Tensor.add(a, b, null));
    }

    @Test
    void testMultiply() {
        double[][] dataA = {{1, 2}, {3, 4}};
        double[][] dataB = {{2, 3}, {4, 5}};
        Tensor a = new Tensor(dataA, 2, 2);
        Tensor b = new Tensor(dataB, 2, 2);

        Tensor result = Tensor.multiply(a, b, null);

        assertEquals(2, result.rows);
        assertEquals(2, result.cols);
        assertEquals(2, result.data[0][0]);
        assertEquals(6, result.data[0][1]);
        assertEquals(12, result.data[1][0]);
        assertEquals(20, result.data[1][1]);
    }


    @Test
    void testMultiplyShapeMismatch() {
        Tensor a = Tensor.zeros(2, 2);
        Tensor b = Tensor.zeros(3, 2);
        assertThrows(IllegalArgumentException.class, () -> Tensor.multiply(a, b, null));
    }

    @Test
    void testMatMul() {
        double[][] dataA = {{1, 2, 3}, {4, 5, 6}};
        double[][] dataB = {{7, 8}, {9, 10}, {11, 12}};
        Tensor a = new Tensor(dataA, 2, 3);
        Tensor b = new Tensor(dataB, 3, 2);

        Tensor result = Tensor.matMul(a, b, null);

        assertEquals(2, result.rows);
        assertEquals(2, result.cols);
        assertEquals(58, result.data[0][0]); // 1*7+2*9+3*11
        assertEquals(64, result.data[0][1]); // 1*8+2*10+3*12
        assertEquals(139, result.data[1][0]); // 4*7+5*9+6*11
        assertEquals(154, result.data[1][1]); // 4*8+5*10+6*12
    }

    @Test
    void testMatMulShapeMismatch() {
        Tensor a = Tensor.zeros(2, 3);
        Tensor b = Tensor.zeros(4, 2);
        assertThrows(IllegalArgumentException.class, () -> Tensor.matMul(a, b, null));
    }

    @Test
    void testRelu() {
        double[][] data = {{-1, 2}, {0, -3}};
        Tensor t = new Tensor(data, 2, 2);
        Tensor result = Tensor.Relu(t, null);

        assertEquals(0, result.data[0][0]);
        assertEquals(2, result.data[0][1]);
        assertEquals(0, result.data[1][0]);
        assertEquals(0, result.data[1][1]);
    }

    @Test
    void testSigmoid() {
        double[][] data = {{0}};
        Tensor t = new Tensor(data, 1, 1);
        Tensor result = Tensor.Sigmoid(t, null);
        assertEquals(0.5, result.data[0][0], 1e-9);
    }

    @Test
    void testFill() {
        Tensor t = Tensor.zeros(2, 2);
        t.fill(3.14);
        for (int i = 0; i < t.rows; i++) {
            for (int j = 0; j < t.cols; j++) {
                assertEquals(3.14, t.data[i][j]);
            }
        }
    }

    @Test
    void testSumRows() {
        double[][] data = {{1, 2}, {3, 4}};
        Tensor t = new Tensor(data, 2, 2);
        Tensor sumRows = t.sumRows(null);

        assertEquals(1, sumRows.rows);
        assertEquals(2, sumRows.cols);
        assertEquals(4, sumRows.data[0][0]);
        assertEquals(6, sumRows.data[0][1]);
    }

    @Test
    void testSumCols() {
        double[][] data = {{1, 2}, {3, 4}};
        Tensor t = new Tensor(data, 2, 2);
        Tensor sumCols = t.sumCols(null);

        assertEquals(2, sumCols.rows);
        assertEquals(1, sumCols.cols);
        assertEquals(3, sumCols.data[0][0]);
        assertEquals(7, sumCols.data[1][0]);
    }

    @Test
    void testTranspose() {
        double[][] data = {{1, 2, 3}, {4, 5, 6}};
        Tensor t = new Tensor(data, 2, 3);
        Tensor transposed = t.transpose();

        assertEquals(3, transposed.rows);
        assertEquals(2, transposed.cols);
        assertEquals(1, transposed.data[0][0]);
        assertEquals(4, transposed.data[0][1]);
        assertEquals(2, transposed.data[1][0]);
        assertEquals(5, transposed.data[1][1]);
        assertEquals(3, transposed.data[2][0]);
        assertEquals(6, transposed.data[2][1]);
    }

    @Test
    void testSoftmax() {
        double[][] data = {{1, 2}, {3, 4}};
        Tensor t = new Tensor(data, 2, 2);
        Tensor softmax = Tensor.Softmax(t, null);

        for (int col = 0; col < softmax.cols; col++) {
            double colSum = 0;
            for (int row = 0; row < softmax.rows; row++) {
                colSum += softmax.data[row][col];
                assertTrue(softmax.data[row][col] >= 0 && softmax.data[row][col] <= 1);
            }
            assertEquals(1.0, colSum, 1e-9);
        }
    }
}
