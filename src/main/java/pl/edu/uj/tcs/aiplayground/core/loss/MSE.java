package pl.edu.uj.tcs.aiplayground.core.loss;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

public class MSE implements LossFunc {
    public double loss(Tensor pred, Tensor Y) {
        double loss = 0;
        for (int i = 0; i < pred.rows; i++) {
            for (int j = 0; j < pred.cols; j++) {
                loss += Math.pow(pred.data[i][j] - Y.data[i][j], 2);
                pred.gradient[i][j] += 2 * (pred.data[i][j] - Y.data[i][j]);
            }

        }
        return loss;
    }
}
