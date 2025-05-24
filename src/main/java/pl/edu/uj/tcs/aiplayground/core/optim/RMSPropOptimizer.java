package pl.edu.uj.tcs.aiplayground.core.optim;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

import java.util.ArrayList;

public class RMSPropOptimizer extends Optimizer {
    double decayRate = 0.9;
    double epsilon = 1e-8;

    ArrayList<double[][]> cache;

    public RMSPropOptimizer(ArrayList<Tensor> params, double learningRate) {
        this.params = params;
        this.learningRate = learningRate;
        this.cache = new ArrayList<>();

        for (Tensor param : params) {
            cache.add(new double[param.rows][param.cols]);
        }
    }

    public void optimize() {
        for (int i = 0; i < params.size(); i++) {
            Tensor param = params.get(i);
            double[][] grad = param.gradient;
            double[][] cache_t = cache.get(i);

            for (int r = 0; r < param.rows; r++) {
                for (int c = 0; c < param.cols; c++) {
                    cache_t[r][c] = decayRate * cache_t[r][c] + (1 - decayRate) * grad[r][c] * grad[r][c];
                    param.data[r][c] -= learningRate * grad[r][c] / (Math.sqrt(cache_t[r][c]) + epsilon);
                }
            }
        }
    }
}
