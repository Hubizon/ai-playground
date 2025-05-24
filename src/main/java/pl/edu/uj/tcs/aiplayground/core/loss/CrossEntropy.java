package pl.edu.uj.tcs.aiplayground.core.loss;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

public class CrossEntropy implements LossFunc {
    @Override
    public double loss(Tensor pred, Tensor Y) {
        double loss = 0;
        int batchSize = pred.rows;
        int numClasses = pred.cols;

        for (int i = 0; i < batchSize; i++) {
            for (int j = 0; j < numClasses; j++) {
                double p = pred.data[i][j];
                double y = Y.data[i][j];
                // Clamp p to avoid log(0)
                p = Math.max(Math.min(p, 1 - 1e-7), 1e-7);
                loss += -y * Math.log(p);

                // Gradient for softmax + cross-entropy:
                pred.gradient[i][j] = p - y;
            }
        }
        return loss;
    }
}