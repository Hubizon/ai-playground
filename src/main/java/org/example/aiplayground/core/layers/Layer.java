package org.example.aiplayground.core.layers;

import org.example.aiplayground.core.ComputationalGraph;
import org.example.aiplayground.core.Tensor;

import java.util.ArrayList;

public interface Layer {
    public Tensor forward(Tensor input, ComputationalGraph graph);
    public ArrayList<Tensor> getParams();
}
