package org.example.aiplayground.core;

import java.util.ArrayList;

public class Utils {
    public static double[] matmul(double[] a, ArrayList<Integer> shapeA, double[] b, ArrayList<Integer> shapeB) {
        int aRows = shapeA.get(0);
        int aCols = shapeA.get(1);
        int bRows = shapeB.get(0);
        int bCols = shapeB.get(1);

        if (aCols != bRows) {
            throw new IllegalArgumentException("Matrix dimensions do not align: " +
                    aCols + " (A columns) vs " + bRows + " (B rows).");
        }

        double[] result = new double[aRows * bCols];

        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bCols; j++) {
                double sum = 0.0;
                for (int k = 0; k < aCols; k++) {
                    sum += a[i * aCols + k] * b[k * bCols + j];
                }
                result[i * bCols + j] = sum;
            }
        }

        return result;
    }

}
