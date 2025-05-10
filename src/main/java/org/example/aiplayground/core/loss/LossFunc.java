package org.example.aiplayground.core.loss;

import org.example.aiplayground.core.Tensor;

public interface LossFunc {
    public double loss(Tensor pred, Tensor Y);
}
