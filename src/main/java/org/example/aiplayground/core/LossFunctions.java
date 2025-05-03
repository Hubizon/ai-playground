package org.example.aiplayground.core;

public class LossFunctions {

    public static double MSE(Tensor c, Tensor Y) {
        double loss =0;
        for(int i=0;i<c.data.length;i++)
        {
            loss+=Math.pow(c.data[i] - Y.data[i], 2);
            c.gradient[i] += 2 * (c.data[i] - Y.data[i]);
        }
        return loss;
    }

}
