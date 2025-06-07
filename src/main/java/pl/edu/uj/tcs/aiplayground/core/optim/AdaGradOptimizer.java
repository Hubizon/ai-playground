package pl.edu.uj.tcs.aiplayground.core.optim;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

import java.util.ArrayList;

public class AdaGradOptimizer extends Optimizer {
    double epsilon = 1e-8;
    ArrayList<double[][]> gradSums;

    public AdaGradOptimizer(ArrayList<Tensor> params, double learningRate) {
        this.params = params;
        this.learningRate = learningRate;
        this.gradSums = new ArrayList<>();

        for (Tensor param : params) {
            gradSums.add(new double[param.rows][param.cols]);
        }
    }

    public void optimize() {
        for (int i = 0; i < params.size(); i++) {
            Tensor param = params.get(i);
            double[][] grad = param.gradient;
            double[][] sumSq = gradSums.get(i);

            for (int r = 0; r < param.rows; r++) {
                for (int c = 0; c < param.cols; c++) {
                    sumSq[r][c] += grad[r][c] * grad[r][c];
                    param.data[r][c] -= learningRate * grad[r][c] / (Math.sqrt(sumSq[r][c]) + epsilon);
                }
            }
        }
    }
}
