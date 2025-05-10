package org.example.aiplayground.core.layers;

import org.example.aiplayground.core.ComputationalGraph;
import org.example.aiplayground.core.Tensor;
import org.json.JSONObject;

import java.util.ArrayList;

public class SigmoidLayer implements Layer {
    public SigmoidLayer() {}
    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.Sigmoid(input,graph);
    }
    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }
    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "SigmoidLayer");
        return json.toString();
    }
}