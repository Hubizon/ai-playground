package pl.edu.uj.tcs.aiplayground.core.loss;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

public class BCE implements LossFunc {
    public double loss(Tensor pred, Tensor Y) {
        double loss = 0;
        for (int i = 0; i < pred.rows; i++) {
            for (int j = 0; j < pred.cols; j++) {
                double p = pred.data[i][j];
                double y = Y.data[i][j];
                p = Math.max(Math.min(p, 1 - 1e-7), 1e-7);
                loss += -(y * Math.log(p) + (1 - y) * Math.log(1 - p));
                pred.gradient[i][j] = (p - y);
            }
        }
        return loss;
    }
}