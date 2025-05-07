package org.example.aiplayground.core;

public class LossFunctions {

    public static double MSE(Tensor c, Tensor Y) {
        double loss =0;
        for(int i=0;i<c.rows;i++)
        {
            for(int j=0;j<c.cols;j++)
            {
                loss+=Math.pow(c.data[i][j] - Y.data[i][j], 2);
                c.gradient[i][j] += 2 * (c.data[i][j] - Y.data[i][j]);
            }

        }
        return loss;
    }
    public static double BCE(Tensor c, Tensor Y) {
        double loss = 0;
        for (int i = 0; i < c.rows; i++) {
            for (int j = 0; j < c.cols; j++) {
                double p = c.data[i][j];
                double y = Y.data[i][j];

                // Avoiding log(0) by clamping values to a small range
                p = Math.max(Math.min(p, 1 - 1e-7), 1e-7);

                // Calculating loss
                loss += -(y * Math.log(p) + (1 - y) * Math.log(1 - p));

                // Calculating gradient
                c.gradient[i][j] += (p - y) / (p * (1 - p));
            }
        }
        return loss;
    }
}
