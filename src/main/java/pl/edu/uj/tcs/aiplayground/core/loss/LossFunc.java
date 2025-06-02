package pl.edu.uj.tcs.aiplayground.core.loss;

import pl.edu.uj.tcs.aiplayground.core.Tensor;

public interface LossFunc {
    double loss(Tensor pred, Tensor Y);
}
