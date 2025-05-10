package org.example.aiplayground.core;


import org.example.aiplayground.core.layers.Layer;

import java.util.ArrayList;
import java.util.List;

public class NeuralNet {

    public ArrayList<Layer> layers = new ArrayList<>();

    public ArrayList<Tensor> getParams() {
        ArrayList<Tensor> params = new ArrayList<>();
        for(Layer layer : layers) {
            params.addAll(layer.getParams());
        }
        return params;
    }

    public Tensor forward(Tensor input, ComputationalGraph graph) {
        Tensor forwardTensor = input;
        for(Layer layer : layers) {
            forwardTensor = layer.forward(forwardTensor, graph);
        }
        return forwardTensor;
    }

}
