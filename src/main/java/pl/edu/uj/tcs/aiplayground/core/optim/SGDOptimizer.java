package pl.edu.uj.tcs.aiplayground.core.optim;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

import java.util.ArrayList;

public class SGDOptimizer extends Optimizer {
    public SGDOptimizer(ArrayList<Tensor> params, double learningRate) {
        this.params = params;
        this.learningRate = learningRate;
    }

    public void optimize() {
        for (Tensor param : params) {
            for (int i = 0; i < param.rows; i++) {
                for (int j = 0; j < param.cols; j++) {
                    param.data[i][j] -= param.gradient[i][j] * learningRate;
                }
            }

        }
    }
}