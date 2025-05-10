package pl.edu.uj.tcs.aiplayground.core.optim;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Optimizer {
    ArrayList<Tensor> params;
    double learningRate;

    public Optimizer() {
        params = new ArrayList<>();
        learningRate = 0.1;
    }

    public Optimizer(ArrayList<Tensor> params, double learningRate) {
        this.params = params;
        this.learningRate = learningRate;
    }

    public abstract void optimize();

    public void zeroGradient() {
        for (Tensor param : params) {
            for (int i = 0; i < param.rows; i++) {
                Arrays.fill(param.gradient[i], 0.0);
            }
        }
    }
}

