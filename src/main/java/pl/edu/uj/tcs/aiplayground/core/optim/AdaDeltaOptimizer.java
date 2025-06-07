package pl.edu.uj.tcs.aiplayground.core.optim;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

import java.util.ArrayList;

public class AdaDeltaOptimizer extends Optimizer {
    double rho = 0.95;
    double epsilon = 1e-6;

    ArrayList<double[][]> Eg;
    ArrayList<double[][]> Edx;

    public AdaDeltaOptimizer(ArrayList<Tensor> params, double learningRate) {
        this.params = params;
        this.learningRate = learningRate;
        this.Eg = new ArrayList<>();
        this.Edx = new ArrayList<>();

        for (Tensor param : params) {
            Eg.add(new double[param.rows][param.cols]);
            Edx.add(new double[param.rows][param.cols]);
        }
    }

    public void optimize() {
        for (int i = 0; i < params.size(); i++) {
            Tensor param = params.get(i);
            double[][] grad = param.gradient;
            double[][] Eg_t = Eg.get(i);
            double[][] Edx_t = Edx.get(i);

            for (int r = 0; r < param.rows; r++) {
                for (int c = 0; c < param.cols; c++) {
                    Eg_t[r][c] = rho * Eg_t[r][c] + (1 - rho) * grad[r][c] * grad[r][c];
                    double update = Math.sqrt(Edx_t[r][c] + epsilon) / Math.sqrt(Eg_t[r][c] + epsilon) * grad[r][c];
                    param.data[r][c] -= update;
                    Edx_t[r][c] = rho * Edx_t[r][c] + (1 - rho) * update * update;
                }
            }
        }
    }
}
