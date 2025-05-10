package org.example.aiplayground.core.layers;

import org.example.aiplayground.core.ComputationalGraph;
import org.example.aiplayground.core.Tensor;

import java.util.ArrayList;

public class ReluLayer implements Layer {
    public ReluLayer() {}
    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.Relu(input,graph);
    }
    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }
}
