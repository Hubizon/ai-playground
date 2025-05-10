package org.example.aiplayground.core.layers;

import org.example.aiplayground.core.ComputationalGraph;
import org.example.aiplayground.core.Tensor;

public interface Layer {
    public Tensor forward(Tensor input, ComputationalGraph graph);
}
