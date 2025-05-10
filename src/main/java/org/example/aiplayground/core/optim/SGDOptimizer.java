package org.example.aiplayground.core.optim;

import org.example.aiplayground.core.Tensor;

public class SGDOptimizer extends Optimizer
{
    public void optimize() {
        for (Tensor param : params) {
            for (int i = 0; i < param.rows; i++) {
                for(int j=0;j<param.cols;j++)
                {
                    param.data[i][j] -= param.gradient[i][j] * learningRate;
                }
            }

        }
    }
}