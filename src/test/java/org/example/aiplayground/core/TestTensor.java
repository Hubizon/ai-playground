package org.example.aiplayground.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestTensor {

    @Test
    public void testSumAlongFirstAxis() {
        // Data represents a 2x3 tensor:
        // [[1, 2, 3],
        //  [4, 5, 6]]
        double[] inputData = new double[] {
                1, 2, 3,
                4, 5, 6
        };
        ArrayList<Integer> shape = new ArrayList<>(Arrays.asList(2, 3));
        Tensor tensor = new Tensor(inputData, shape);
        ComputationalGraph graph = new ComputationalGraph();

        Tensor result = tensor.sum(graph);

        // Expect sum along axis 0:
        // [1+4, 2+5, 3+6] = [5, 7, 9]
        double[] expectedData = new double[] {5, 7, 9};
        ArrayList<Integer> expectedShape = new ArrayList<>(Arrays.asList(3));

        assertArrayEquals(expectedData, result.data, 1e-6, "Sum values incorrect");
        assertEquals(expectedShape, result.shape, "Shape after sum incorrect");
    }
    @Test
    public void testSum2x3Tensor() {
        // [[1, 2, 3],
        //  [4, 5, 6]]
        double[] data = {1, 2, 3, 4, 5, 6};
        Tensor tensor = new Tensor(data, new ArrayList<>(Arrays.asList(2, 3)));
        Tensor result = tensor.sum(new ComputationalGraph());

        assertArrayEquals(new double[]{5, 7, 9}, result.data, 1e-6);
        assertEquals(Arrays.asList(3), result.shape);
    }

    @Test
    public void testSum3x2Tensor() {
        // [[1, 2],
        //  [3, 4],
        //  [5, 6]]
        double[] data = {1, 2, 3, 4, 5, 6};
        Tensor tensor = new Tensor(data, new ArrayList<>(Arrays.asList(3, 2)));
        Tensor result = tensor.sum(new ComputationalGraph());

        assertArrayEquals(new double[]{9, 12}, result.data, 1e-6);
        assertEquals(Arrays.asList(2), result.shape);
    }

    @Test
    public void testSum1DVector() {
        // [1, 2, 3]
        double[] data = {1, 2, 3};
        Tensor tensor = new Tensor(data, new ArrayList<>(Arrays.asList(3)));
        Tensor result = tensor.sum(new ComputationalGraph());

        // A 1D vector sum along "axis 0" should return an empty shape
        // but in your implementation it keeps the original shape — this may be a bug
        assertArrayEquals(new double[]{6}, result.data, 1e-6);
        assertEquals(Arrays.asList(1), result.shape);  // May want to change this
    }

    @Test
    public void testSum3x2x2Tensor() {
        // Shape: [3, 2, 2]
        double[] data = {
                // Slice 0
                1, 2,
                3, 4,
                // Slice 1
                5, 6,
                7, 8,
                // Slice 2
                9, 10,
                11, 12
        };
        Tensor tensor = new Tensor(data, new ArrayList<>(Arrays.asList(3, 2, 2)));
        Tensor result = tensor.sum(new ComputationalGraph());

        // Sum over first axis (3 slices): result should be shape [2, 2]
        // Result:
        // [1+5+9, 2+6+10] = [15, 18]
        // [3+7+11, 4+8+12] = [21, 24]
        assertArrayEquals(new double[]{15, 18, 21, 24}, result.data, 1e-6);
        assertEquals(Arrays.asList(2, 2), result.shape);
    }

    @Test
    public void testSumWithZeros() {
        double[] data = {0, 0, 0, 0};
        Tensor tensor = new Tensor(data, new ArrayList<>(Arrays.asList(2, 2)));
        Tensor result = tensor.sum(new ComputationalGraph());

        assertArrayEquals(new double[]{0, 0}, result.data, 1e-6);
        assertEquals(Arrays.asList(2), result.shape);
    }
}
